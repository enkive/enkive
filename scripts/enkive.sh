#!/bin/sh

ENKIVE_OWNER=enkive
ENKIVE_HOME=/opt/enkive/
ENKIVE_CLASSPATH=$ENKIVE_HOME/enkive.jar:$ENKIVE_HOME/lib/*:$ENKIVE_HOME/lib/spring/*:$ENKIVE_HOME/config/
JSVC=/usr/local/bin/jsvc
JAVA_HOME=/usr/lib/jvm/java-openjdk
INDRI_SO_PATH=/usr/local/lib/

start() {
        echo -n "Starting Enkive:  "
        su $ENKIVE_OWNER -c "$JSVC -java-home $JAVA_HOME -pidfile $ENKIVE_HOME/enkive.pid -errfile $ENKIVE_HOME/log/enkive.err -cp $ENKIVE_CLASSPATH -Djava.library.path=$INDRI_SO_PATH com.linuxbox.enkive.EnkiveDaemon"
        sleep 2
}
stop() {
        echo -n "Stopping Enkive: "
        su $ENKIVE_OWNER -c "$JSVC -stop -java-home $JAVA_HOME -pidfile $ENKIVE_HOME/enkive.pid -errfile $ENKIVE_HOME/log/enkive.err -cp $ENKIVE_CLASSPATH -Djava.library.path=$INDRI_SO_PATH com.linuxbox.enkive.EnkiveDaemon"
}

# See how we were called.
case "$1" in
  start)
        start
        ;;
  stop)
        stop
        ;;
  restart)
        stop
        start
        ;;
  *)
        echo $"Usage: enkive {start|stop|restart}"
        exit
esac
