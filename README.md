# Eclipselink 2.6.4 LEFT JOIN TREAT bug

This project runs two tests.
A placebo test that passes showing a simple query with a reuglar left join.
A second test that fails that uses a LEFT JOIN TREAT query.
The test that fails is not working becuase eclipselink JPQL to NativeSQL is not compiling an approrpriate query predicate on the TREAT
table.
In particular, the predicate is too strong forcing an equals on the descriminator value and not allowing for NULL relationship.
In additiona, an equals in the descriminator value is incorrect behavior.
An IN query should be used, in case the TREAT AS entity has sub-classes.


```
        bind => [6, Batmam, 1, null, SuperHero]
[EL Fine]: sql: 2016-12-06 11:02:22.991--ClientSession(1689718593)--Connection(779511842)--Thread(Thread[main,5,main])--INSERT INTO HUMAN (ID, HUMAN_NAME, VERSI
ON, PARENT_ID, CLASS_TYPE) VALUES (?, ?, ?, ?, ?)
        bind => [8, RegularDaddy, 1, null, Human]
[EL Fine]: sql: 2016-12-06 11:02:22.992--ClientSession(1689718593)--Connection(779511842)--Thread(Thread[main,5,main])--INSERT INTO HUMAN (ID, HUMAN_NAME, VERSI
ON, PARENT_ID, CLASS_TYPE) VALUES (?, ?, ?, ?, ?)
        bind => [9, BatmanChild, 1, 6, Human]
[EL Fine]: sql: 2016-12-06 11:02:22.994--ClientSession(1689718593)--Connection(779511842)--Thread(Thread[main,5,main])--INSERT INTO HUMAN (ID, HUMAN_NAME, VERSI
ON, PARENT_ID, CLASS_TYPE) VALUES (?, ?, ?, ?, ?)
        bind => [10, RegularChild, 1, 8, Human]
[EL Fine]: sql: 2016-12-06 11:02:23.019--ClientSession(1689718593)--Connection(779511842)--Thread(Thread[main,5,main])--SELECT t1.ID, t1.CLASS_TYPE, t1.HUMAN_NA
ME, t1.VERSION, t1.PARENT_ID FROM HUMAN t1 LEFT OUTER JOIN HUMAN t0 ON (t0.ID = t1.PARENT_ID) WHERE ((1 = 1) AND (t0.CLASS_TYPE = 'SuperHero'))
THe Human we have in the BUGY left join query  is the: BatmanChild
Tests run: 2, Failures: 1, Errors: 0, Skipped: 0, Time elapsed: 2.971 sec <<< FAILURE! - in jpa.eclipselink.test.dao.LeftJoinBugTest
leftJoinTreatQueryBugTest(jpa.eclipselink.test.dao.LeftJoinBugTest)  Time elapsed: 0.046 sec  <<< FAILURE!
java.lang.AssertionError: expected:<4> but was:<1>
        at org.junit.Assert.fail(Assert.java:88)
        at org.junit.Assert.failNotEquals(Assert.java:834)
        at org.junit.Assert.assertEquals(Assert.java:645)
        at org.junit.Assert.assertEquals(Assert.java:631)
        at jpa.eclipselink.test.dao.LeftJoinBugTest.leftJoinTreatQueryBugTest(LeftJoinBugTest.java:124)


Results :

Failed tests:
  LeftJoinBugTest.leftJoinTreatQueryBugTest:124 expected:<4> but was:<1>

Tests run: 2, Failures: 1, Errors: 0, Skipped: 0

[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 7.466 s
[INFO] Finished at: 2016-12-06T11:02:23+01:00
[INFO] Final Memory: 20M/266M
[INFO] ------------------------------------------------------------------------
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-surefire-plugin:2.17:test (default-test) on project left-join-treat-bug: There are test failures.



```
