@ECHO off

SET GLASSFISH_ASADMIN=C:\glassfishv3\glassfish\bin\asadmin.bat

ECHO ---------------------------------------------------
ECHO Local database and Realm
ECHO ---------------------------------------------------
ECHO.
ECHO Register the JDBC connection pool
call %GLASSFISH_ASADMIN% create-jdbc-connection-pool --driverclassname com.mysql.jdbc.Driver --restype java.sql.Driver --property url=jdbc\:mysql\://localhost\:3306/osznconnection:user=osznconnection:password=osznconnection osznconnectionPool

ECHO.
ECHO Create a JDBC resource with the specified JNDI name
call %GLASSFISH_ASADMIN% create-jdbc-resource --connectionpoolid osznconnectionPool jdbc/osznconnectionResource

ECHO.
ECHO Add the named authentication realm
call %GLASSFISH_ASADMIN% create-auth-realm --classname com.sun.enterprise.security.auth.realm.jdbc.JDBCRealm --property jaas-context=jdbcRealm:datasource-jndi=jdbc/osznconnectionResource:user-table=user:user-name-column=login:password-column=password:group-table=usergroup:group-name-column=group_name:charset=UTF-8 osznconnectionRealm
 
ECHO.
ECHO ---------------------------------------------------
ECHO Remote database
ECHO ---------------------------------------------------
ECHO.
ECHO Register the JDBC connection pool
call %GLASSFISH_ASADMIN% create-jdbc-connection-pool --driverclassname oracle.jdbc.OracleDriver --restype java.sql.Driver --property url=jdbc\:oracle\:thin\:@192.168.1.102\:1521\:cnhar:user=comp:password=comp osznconnectionRemotePool

ECHO.
ECHO Create a JDBC resource with the specified JNDI name
call %GLASSFISH_ASADMIN% create-jdbc-resource --connectionpoolid osznconnectionPool jdbc/osznconnectionRemoteResource