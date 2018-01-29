# Replan Optimizer
The optimizer has the purpose of generating a release plan. It has been developed as a stateless web service that given all the required information generates a release plan that preserving the stated constraints optimizes the use of the company resources to develop the next release. 

## Installation
The following steps describe the installation procedure for the Replan optimizer v2

### Compilation instructions

1. Clone the SUPERSEDE Replan Git repository.
 * `git clone https://github.com/supersede-project/replan_optimizer_v2`
2. Build the project.
 * `./gradlew build`

### Installation instructions
1. Copy the dashboard war file to the Tomcat directory.
 * `cp <generated WAR> <CATALINA_HOME>/webapps/`
2. Run Tomcat. 
 * `cd <CATALINA_HOME>/bin/`
 * `./start_up.sh`

Contact: David Ameller <dameller@essi.upc.edu>

