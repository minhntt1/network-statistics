## ADDED Requirements

### Requirement: Deployment guide covers local development setup
The deployment guide SHALL provide local development instructions:
- Prerequisites (Java 21, MySQL, Git)
- Database setup (creating schemas and tables)
- Building the project with Gradle
- Running with dev profiles from command line

#### Scenario: Local setup steps are documented
- **WHEN** a developer follows the local deployment guide
- **THEN** they SHALL be able to set up and run the application on their local machine

### Requirement: Deployment guide covers container deployment
The deployment guide SHALL document container-based deployment:
- Docker image build process
- Environment variable configuration for prd profiles
- Container networking (hostname-based service discovery)

#### Scenario: Container deployment steps are documented
- **WHEN** a developer follows the container deployment guide
- **THEN** they SHALL understand how to deploy the application using containers

### Requirement: Deployment guide covers production procedures
The deployment guide SHALL document production deployment procedures:
- Branch strategy (feature branches → dev → prd → main)
- Testing and validation steps
- Monitoring and rollback procedures

#### Scenario: Production workflow is documented
- **WHEN** a developer reads the deployment guide
- **THEN** they SHALL understand the release workflow from development to production
