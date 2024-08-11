The current version of Google's Buildpack supports several different version of Java. The Latest LTS that it supports is Java 17 so we'll start there. The examples tend towards using Mavem so that makes the most sense as well to embrace it.

I want to be able to run things locally as well as in built docker containers so I want to start with a local setup. Obviously I already have `brew` installed, so...
`brew install openjdk@17`
`brew install maven`

This is the message that the java install spits out, keeping it for now.

```
For the system Java wrappers to find this JDK, symlink it with
  sudo ln -sfn /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk

openjdk@17 is keg-only, which means it was not symlinked into /opt/homebrew,
because this is an alternate version of another formula.

If you need to have openjdk@17 first in your PATH, run:
  echo 'export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
```

The `brew` installation of `maven` comes with `openjdk@22` and I could tell it not to install that, but naw the more the merrier.
You can see that both of them have been succesfully installed with:
`/opt/homebrew/opt/openjdk@17/bin/java --version`
`/opt/homebrew/opt/openjdk@22/bin/java --version`

Register the version of Java from brew
`sudo ln -sfn /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk`
`sudo ln -sfn /opt/homebrew/opt/openjdk@22/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-22.jdk`

Confirm they are registered with the system
`/usr/libexec/java_home -V`

Add this line to `~/.zshrc`
`` export JAVA_HOME=`/usr/libexec/java_home -v 17` ``

Now verify that openjdk@17 is registered with maven
`mvn --version`

Build the application
`mvn clean package -e`

Run the application
`java -jar target/filmlinkd.jar`

The easiest but most insecure way to allow communication with GCP services is via credentials secret
`export GOOGLE_APPLICATION_CREDENTIALS="/Users/jlind/Developer/filmlinkd-java/.gcp-key.json"`

Build and run the application locally
`mvn package && java -jar target/filmlinkd.jar`

Let's get dangerous....
`pack build filmlinkd-java-app --builder=gcr.io/buildpacks/builder:google-22 --env GOOGLE_RUNTIME_VERSION=17`

Unfortnatly that doesn't work because it doesn't have the credentials it needs to actually run anything on GCP but the fact that it complains about not having credentials seems promising.
