GLASSFISH_ASADMIN=C:\glassfishv3\glassfish\bin\asadmin

echo ---------------------------------------------------
echo Local database and Realm
echo ---------------------------------------------------
echo
echo Register the JDBC connection pool
exec $GLASSFISH_ASADMIN create-jdbc-connection-pool --driverclassname com.mysql.jdbc.Driver --restype java.sql.Driver --property url=jdbc\:mysql\://localhost\:3306/osznconnection:user=osznconnection:password=osznconnection osznconnectionPool

echo
echo Create a JDBC resource with the specified JNDI name
exec $GLASSFISH_ASADMIN create-jdbc-resource --connectionpoolid osznconnectionPool jdbc/osznconnectionResource

echo
echo Add the named authentication realm
exec $GLASSFISH_ASADMIN create-auth-realm --classname com.sun.enterprise.security.auth.realm.jdbc.JDBCRealm --property jaas-context=jdbcRealm:datasource-jndi=jdbc/osznconnectionResource:user-table=user:user-name-column=login:password-column=password:group-table=usergroup:group-name-column=group_name:charset=UTF-8 osznconnectionRealm
 
echo
echo ---------------------------------------------------
echo Remote database
echo ---------------------------------------------------
echo
echo Register the JDBC connection pool
exec $GLASSFISH_ASADMIN create-jdbc-connection-pool --driverclassname oracle.jdbc.OracleDriver --restype java.sql.Driver --property url=jdbc\:oracle\:thin\:@192.168.1.102\:1521\:cnhar:user=comp:password=comp osznconnectionRemotePool

echo
echo Create a JDBC resource with the specified JNDI name
exec $GLASSFISH_ASADMIN create-jdbc-resource --connectionpoolid osznconnectionPool jdbc/osznconnectionRemoteResource