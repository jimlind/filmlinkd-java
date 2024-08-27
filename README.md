# filmlinkd-java

Experimental Java App for Running Filmlinkd Processes
You probably don't want this one.

Achievements of this project right now.

- Java 17 Running Locally
- Writing Scheduled Messages to a Hardcoded Discord Channel
- Buildpack Running Locally and and in Cloud Build
- Docker Container Running in Google Compute Engine
- Writing to Google Logs
- Listen for PubSub messages
- Post Something to Discord
- Setup Spring Boot for Dependency Injection
- Setup production and development configs
- Rework production and development environments
- Don't use the env flags during cloudbuild
- Solve sharding
- Post the right message to the right channel
- Create subscription if it doesn't exist
- Don't blow up if subscription already exists
- Reuse one subscription for all shards
- Shutdown pubsub on exit
- Use annotation for logger
- Disconnect discord on exit.

Next.

- Build a proper Discord Embed Message
- Update database after embed written
- Make sure thing restarts if it fails
