A collection of Andalite recipes for general use.

## Configuration

Add an entry for the Andalite plugin to your `pom.xml` file, and include the Fully Qualified Domain Name of the recipes you want to use:

```xml

<build>
	<pluginManagement>
		<plugins>
			<plugin>
				<groupId>com.jeroensteenbeeke.andalite</groupId>
				<artifactId>andalite-maven-plugin</artifactId>
				<configuration>
					<recipes>
						<recipe>com.jeroensteenbeeke.andalite.recipes.JSR305Transformation</recipe>
					</recipes>
				</configuration>
				<dependencies>
					<dependency>
						<groupId>com.jeroensteenbeeke.andalite</groupId>
						<artifactId>andalite-recipes</artifactId>
						<version>1.0-SNAPSHOT</version>
					</dependency>
				</dependencies>
			</plugin>
		</plugins>
	</pluginManagement>
</build>
```

Please be adviced that at this time, neither these recipes nor Andalite itself are available from Maven Central. Once Andalite reaches a stable release, we expect this to change.

## Usage

From the directory in which your `pom.xml` is located, run the following command:

```bash

$ mvn andalite:forge

```

And then use the menu to select the recipe to run
