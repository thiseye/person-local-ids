# person-local-ids

Instructions:

- Create a mysql database with 'public' schema, root user and root password with table 'Person' (or update MySqlConnection.scala with your database's information)

```CREATE TABLE `Person` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `localId` varchar(45) NOT NULL,
  `localIdType` int(11) NOT NULL,
  `personId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=latin1```

- Modify AppRunner.scala with path to test CSV file.
- Install sbt and run 'sbt run' from the project root

Approach: 

- Immediately, a graph database came to mind as a good candidate for this exercise, but not having used one, I didn't want to waste time getting acclimated, so I went with a relational database.
- The provided solution should handle a moderate amount of data very quickly. For handling a larger amount, I wanted to parallize and scale out using Akka but knew I wouldn't have time for that in a few hours.
- The app is currently single-threaded otherwise it'd surely run into data race issues. It should use transactions or other locking mechanism to maintain consistency should there be more than one reader/writer in the future.
- The general description of the algorithm is to check if any of the local IDs on a given row are already in the database. If there are, use the lowest person ID of those entries, otherwise assign a new unique numeric ID. Update any rows that have a person ID that is not equal to this ID. Insert any local IDs that aren't already in the database.
- Normally, I'd have unit tests, but I ran out of time to write them and to do other testing.
