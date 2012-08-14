package com.linuxbox.enkive.statistics.gathering.consolidated;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_ATTACH_NUM;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_ATTACH_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_GATHERER_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIMESTAMP;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_LENGTH;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_UPLOAD_DATE;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_AVG;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_DAY;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_HOUR;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MAX;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MIN;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MONTH;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_TYPE;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_WEEK;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import com.linuxbox.enkive.statistics.StatsQuery;
import com.linuxbox.enkive.statistics.services.StatsClient;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class AttachmentsConsolidatedGatherer{
	StatsClient client;
	Mongo m;
	DB db;
	DBCollection attachmentsColl;
	String gathererName;
	final Date startDate;
	final Date endDate;
	private int hrKeepTime;
	private int dayKeepTime;
	private int wkKeepTime;
	private int monthKeepTime;
	
	public AttachmentsConsolidatedGatherer(Mongo m, String dbName, String attachmentsColl, String statisticsColl, String gathererName, StatsClient client, int hrKeepTime, int dayKeepTime, int wkKeepTime, int monthKeepTime) {
		this.client = client;
		this.m = m;
		this.db = m.getDB(dbName);
		this.attachmentsColl = db.getCollection(attachmentsColl + ".files");
		this.gathererName = gathererName;
		this.hrKeepTime = hrKeepTime;
		this.dayKeepTime = dayKeepTime;
		this.wkKeepTime = wkKeepTime;
		this.monthKeepTime = monthKeepTime;
		startDate = getEarliestAttachmentDate();
		endDate = getEarliestStatisticDate();
	}
	
	@PostConstruct
	public void init(){
		System.out.println("Start: " + new Date());
		consolidatePastHours();
		consolidatePastDays();
		consolidatePastWeeks();
		consolidatePastMonths();
		System.out.println("End: " + new Date());
	}
	
	protected Date getEarliestAttachmentDate(){
		DBObject sort = new BasicDBObject(MONGO_UPLOAD_DATE, 1);
		return (Date)attachmentsColl.find().sort(sort).next().get(MONGO_UPLOAD_DATE);
	}
	
	protected void consolidatePastHours(){
		consolidatePast(GRAIN_HOUR);
	}
	
	protected void consolidatePastDays(){
		consolidatePast(GRAIN_DAY);
	}
	
	protected void consolidatePastWeeks(){
		consolidatePast(GRAIN_WEEK);
	}
	
	protected void consolidatePastMonths(){
		consolidatePast(GRAIN_MONTH);
	}
	
	private Calendar getStartCalendar(int grain){
		Calendar startCalendar = Calendar.getInstance();
		startCalendar.setTime(this.startDate);
		startCalendar.set(Calendar.MINUTE, 0);
		startCalendar.set(Calendar.SECOND, 0);
		startCalendar.set(Calendar.MILLISECOND, 0);
		
		Calendar keepCalendar = Calendar.getInstance();
		keepCalendar.set(Calendar.MINUTE, 0);
		keepCalendar.set(Calendar.SECOND, 0);
		keepCalendar.set(Calendar.MILLISECOND, 0);
		
		
		if(grain == GRAIN_HOUR){
			keepCalendar.add(Calendar.HOUR, -hrKeepTime);
		} else if(grain == GRAIN_DAY){
			startCalendar.set(Calendar.HOUR, 0);
			
			keepCalendar.set(Calendar.HOUR, 0);
			keepCalendar.add(Calendar.DATE,-dayKeepTime); 
		} else if(grain == GRAIN_WEEK){
			startCalendar.set(Calendar.HOUR, 0);
			startCalendar.set(Calendar.DATE, 0);
			
			keepCalendar.set(Calendar.HOUR, 0);
			keepCalendar.set(Calendar.DATE, 0);
			keepCalendar.add(Calendar.WEEK_OF_YEAR, -wkKeepTime);
		} else if(grain == GRAIN_MONTH){
			startCalendar.set(Calendar.HOUR, 0);
			startCalendar.set(Calendar.DATE, 0);
			
			keepCalendar.set(Calendar.HOUR, 0);
			keepCalendar.set(Calendar.DATE, 0);
			keepCalendar.add(Calendar.MONTH,-monthKeepTime);
		}
		
		if(startCalendar.getTimeInMillis() < keepCalendar.getTimeInMillis()){
			startCalendar = keepCalendar;
		}
		
		return startCalendar;
	}
	
	protected Set<Map<String, Object>> consolidatePast(int grain){
		Calendar startCalendar = getStartCalendar(grain);	
		Set<Map<String, Object>> consolidatedMaps = new HashSet<Map<String, Object>>();
		if(startCalendar.getTimeInMillis() < endDate.getTime()){
			Date startDate = startCalendar.getTime();
			Calendar cal = Calendar.getInstance();
			cal.setTime(startDate);
			while(startDate.getTime() < endDate.getTime()){
				Date start = cal.getTime();
				if(grain == GRAIN_HOUR){
					cal.add(Calendar.HOUR, 1);
				} else if(grain == GRAIN_DAY){
					cal.add(Calendar.DATE, 1);
				} else if(grain == GRAIN_WEEK){
					cal.add(Calendar.WEEK_OF_YEAR, 1);
				} else if(grain == GRAIN_MONTH){
					cal.add(Calendar.MONTH, 1);
				}
				
				Date end = cal.getTime(); 
				consolidatedMaps.add(getConsolidatedData(start, end, grain));
				startDate = end;
			}
			System.out.println("grain: " + grain + ": " + consolidatedMaps.size());
		}
		return consolidatedMaps;
	}
	
	@SuppressWarnings("unchecked")
	protected Date getEarliestStatisticDate(){
		StatsQuery query = new StatsQuery("AttachmentStatsService", null);
		Date earliestDate = new Date();
		for(Map<String, Object> statMap: client.queryStatistics(query)){
			if(statMap.get(GRAIN_TYPE) != null){
				Map<String, Object> tsMap = (Map<String, Object>)statMap.get(STAT_TIMESTAMP);
				Date tempDate = (Date)tsMap.get(GRAIN_MIN);
				if(earliestDate.getTime() > tempDate.getTime()){
					earliestDate = tempDate;
				}
			}
		}
		return earliestDate;
	}
	
	protected Map<String, Object> getConsolidatedData(Date start, Date end, int grain){
		Map<String, Object> result = new HashMap<String, Object>();
		Map<String, Object> query = new HashMap<String, Object>();
		Map<String, Object> innerQuery = new HashMap<String, Object>();
		innerQuery.put("$gte", start);
		innerQuery.put("$lt", end);
		query.put(MONGO_UPLOAD_DATE, innerQuery);
		long dataByteSz = 0;
		DBCursor dataCursor = attachmentsColl.find(new BasicDBObject(query));
		
		for(DBObject obj: dataCursor){
			dataByteSz+=(Long)(obj.get(MONGO_LENGTH));
		}
		Map<String,Object> innerNumAttach = new HashMap<String,Object>();
		innerNumAttach.put(GRAIN_AVG, dataCursor.count());
		
		Map<String,Object> innerAttachSz = new HashMap<String,Object>();
		
		long avgAttSz = 0;
		if(dataCursor.count() != 0){
			avgAttSz = dataByteSz/dataCursor.count();
		}
		
		innerAttachSz.put(GRAIN_AVG, avgAttSz);
		
		Map<String, Object> dateMap = new HashMap<String, Object>();
		dateMap.put(GRAIN_MIN, start);
		dateMap.put(GRAIN_MAX, end);
		
		result.put(STAT_ATTACH_SIZE, innerAttachSz);
		result.put(STAT_ATTACH_NUM, innerNumAttach);
		result.put(STAT_TIMESTAMP, dateMap);
		result.put(GRAIN_TYPE, grain);
		result.put(STAT_GATHERER_NAME, "AttachmentStatsService");
		
		return result;
	}
}
