// TestNGListener.java

package com.mongodb.util;

import java.util.*;
import java.net.*;

import com.mongodb.*;

import org.testng.*;
import org.testng.reporters.*;

public class TestNGListener extends TestListenerAdapter {

    public void onConfigurationFailure(ITestResult itr){
        super.onConfigurationFailure( itr );
        _print( itr.getThrowable() );
    }

    public void onTestFailure(ITestResult tr) {
        super.onTestFailure( tr );
        log("F");
    }

    public void onTestSkipped(ITestResult tr) {
        super.onTestSkipped( tr );
        log("S");
    }
    
    public void onTestSuccess(ITestResult tr) {
        super.onTestSuccess( tr );
        log(".");
    }

    private void log(String string) {
        System.out.print(string);
        if ( ++_count % 40 == 0) {
            System.out.println("");
        }
        System.out.flush();
    }

    public void onFinish(ITestContext context){
        System.out.println();

        for ( ITestResult r : context.getFailedTests().getAllResults() ){
            System.out.println(r);
            System.out.println("Exception : ");
            _print( r.getThrowable() );
        }

        _recordResults( context );
    }
    
    private void _recordResults( ITestContext context ) {
        DBObject obj = new BasicDBObject();
        for( ITestResult r : context.getPassedTests().getAllResults() ) {
            obj.put( r.getTestClass().getName() + "." + r.getName(), 
                     r.getEndMillis()-r.getStartMillis() );
        }
        obj.put( "total", context.getEndDate().getTime()-context.getStartDate().getTime() );
        obj.put( "time", System.currentTimeMillis() );

        try {
            Mongo mongo = new Mongo();
            mongo.getDB( "results" ).getCollection( "testng" ).save( obj );
        }
        catch( Exception e ) {
            System.err.println( "\nUnable to save test results to the db." );
        }
    }

    private void _print( Throwable t ){

        int otcount = 0;
        int jlrcount = 0;

        if (t == null) {
            return;
        }

        System.out.println("-" + t.toString()+ "-");

        for ( StackTraceElement e : t.getStackTrace() ){
            if ( e.getClassName().startsWith( "org.testng.")) {
                if (otcount++ == 0) {
                    System.out.println("  " + e + " (with others of org.testng.* omitted)");
                }
            }
            else if (e.getClassName().startsWith( "java.lang.reflect.") || e.getClassName().startsWith("sun.reflect.") ) {
                if (jlrcount++ == 0) {
                    System.out.println("  " + e  + " (with others of java.lang.reflect.* or sun.reflect.* omitted)");
                }
            }
            else {
                System.out.println("  " +  e );
            }
        }

        if (t.getCause() != null) {
            System.out.println("Caused By : ");
        
            _print(t.getCause());
        }

        System.out.println();
    }

    private int _count = 0;
} 
