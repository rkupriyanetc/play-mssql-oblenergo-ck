organization := "mk.obl.ck.energy.gov.ua"

name := "play-mssql-oblenergo-ck"

scalaVersion := "2.11.7"

version := "1.0-SNAPSHOT"

herokuAppName in Compile := "play-authenticate"

val appDependencies = Seq(
  "be.objectify"                   %% "deadbolt-java"           % "2.5.0",
  "com.feth"                       %% "play-authenticate"       % "0.8.1-SNAPSHOT",
  javaJpa,
  ("org.hibernate"                  % "hibernate-entitymanager" % "5.2.2.Final").intransitive(),
  //.exclude("org.apache.httpcomponents", "httpclient")
  "org.dom4j"                       % "dom4j"                   % "2.0.0",
  "org.hibernate"                   % "ejb3-persistence"        % "1.0.2.GA",
  cache,
  javaWs,
  "org.webjars"                     % "bootstrap"               % "3.2.0",
  "org.easytesting"                 % "fest-assert"             % "1.4"              % "test",
  "org.seleniumhq.selenium"         % "selenium-java"           % "2.52.0"           % "test"
)

// add resolver for deadbolt and easymail snapshots
resolvers += Resolver.sonatypeRepo("snapshots")

PlayKeys.externalizeResources := false

// display deprecated or poorly formed Java
javacOptions ++= Seq("-Xlint:unchecked")
javacOptions ++= Seq("-Xlint:deprecation")
javacOptions ++= Seq("-Xdiags:verbose")

//  Uncomment the next line for local development of the Play Authenticate core:
//lazy val playAuthenticate = project.in(file("modules/play-authenticate")).enablePlugins(PlayJava)

lazy val root = project.in(file("."))
  .enablePlugins(PlayJava, PlayEbean)
  .settings(
    libraryDependencies ++= appDependencies
  )
  /* Uncomment the next lines for local development of the Play Authenticate core: */
  //.dependsOn(playAuthenticate)
  //.aggregate(playAuthenticate)


fork in run := true
