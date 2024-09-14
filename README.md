# filmlinkd-java

Experimental Java App for Running Filmlinkd Processes
You probably don't want this one.

Achievements of this project right now.

- Java 17 Running Locally
- Writing Scheduled Messages to a Hardcoded Discord Channel
- Buildpack Running Locally and in Cloud Build
- Docker Container Running in Google Compute Engine
- Writing to Google Logs
- Listen for PubSub messages
- Post Something to Discord
- Setup Spring Boot for Dependency Injection
- Setup production and development configs
- Rework production and development environments
- Don't use the env flags during cloud build
- Solve sharding
- Post the right message to the right channel
- Create subscription if it doesn't exist
- Don't blow up if subscription already exists
- Reuse one subscription for all shards
- Shutdown pubsub on exit
- Use annotation for logger
- Disconnect discord on exit.
- Build a proper Discord Embed Message
- Confirmed that Discord API doesn't seem to care about number of clients
- Use LinkedList in Queue like you should for a queue
- Single Channel Messages Don't Seem to Work
- Different printed date if old enough
- Checked: No Watched Date, No Release Year (Best Served Cold), Usernames with underscores (\_star\_)
- Fixed Haiku Dan formatting
- Make sure thing restarts if it fails
- Update database after embed written

Next.

- Test what happens when "previous" doesn't exist on a user

Open Larger Bugs.

Users __star__ and __zero__ blow up Node.js with Resource id "__zero__" is invalid because it is reserved. that seems to
be coming from Firestore