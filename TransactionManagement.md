# `@TransactionManagement(CONTAINER)` #

## Один метод, один запрос ##

1. Без аннотаций

insert:
```
SET autocommit=0
select @@session.tx_read_only
insert into `test` (`value`) values ('1403168978755')
commit
```

select:
```
SET autocommit=0
select @@session.tx_read_only
select `id` from `test` where `value` = '1403169926882'
commit
```

select exception:
```
SET autocommit=0
select @@session.tx_read_only
select `id` from `test` where `value` = '1403171336579'
rollback
```

2. `@Transactional`

3. `@TransactionAttribute(MANDATORY)`

4. `@TransactionAttribute(REQUIRES_NEW)`

5. `@TransactionAttribute(SUPPORTS)`

6. `@TransactionAttribute(NOT_SUPPORTED)`
```
SET autocommit=1
SET NAMES utf8
SET character_set_results = NULL
select @@session.tx_read_only
select `id` from `test` where `value` = '1403170708375'
```

7. `@TransactionAttribute(NEVER)`
select:
```
SET autocommit=1
SET NAMES utf8
SET character_set_results = NULL
select @@session.tx_read_only
select `id` from `test` where `value` = '1403170977882'
```

select exception:
```
SET autocommit=1
SET NAMES utf8
SET character_set_results = NULL
select @@session.tx_read_only
select `id` from `test` where `value` = '1403171587358'
```

## Один метод, два запроса ##

**testInsertTwoEx**
```
SET autocommit=0
select @@session.tx_read_only
insert into `test` (`value`) values ('1403172113795')
select @@session.tx_read_only
insert into `test` (`value`) values ('14031721137951403172113823')
rollback
```

**testInsertTwoNeverEx**

```
SET autocommit=1
SET NAMES utf8
SET character_set_results = NULL
select @@session.tx_read_only
insert into `test` (`value`) values ('1403172739987')
select @@session.tx_read_only
insert into `test` (`value`) values ('14031727399871403172740016')
```

## Два метода, один бин ##

## Два метода, два бина ##

# `@TransactionManagement(BEAN)` #

**testUserInsertTwoEx**
```
SET autocommit=1
test@192.168.1.10 on test
SET NAMES utf8
SET character_set_results = NULL
select @@session.tx_read_only
insert into `test` (`value`) values ('1403174738874')
select @@session.tx_read_only
insert into `test` (`value`) values ('14031747388741403174738944')
```

**testUserInsertTwoExTr**
```
SET autocommit=0
select @@session.tx_read_only
insert into `test` (`value`) values ('1403175168015')
select @@session.tx_read_only
insert into `test` (`value`) values ('14031751680151403175168078')
rollback
```