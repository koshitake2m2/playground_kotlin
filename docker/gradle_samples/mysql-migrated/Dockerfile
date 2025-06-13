FROM mysql:8

# Copy initialization scripts
COPY ./mysql8/initdb/init.sql /docker-entrypoint-initdb.d/01-init.sql

# Copy migration files
COPY ./mysql-flyway/sql/*.sql /docker-entrypoint-initdb.d/migrations/

# Create migration script that runs after database initialization
COPY ./mysql-migrated/migrate.sh /docker-entrypoint-initdb.d/99-migrate.sh

# Make sure the script is executable
RUN chmod +x /docker-entrypoint-initdb.d/99-migrate.sh

# Set environment variables
ENV MYSQL_ROOT_PASSWORD=password
ENV TZ=Asia/Tokyo