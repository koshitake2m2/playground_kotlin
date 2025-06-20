#!/bin/bash

echo "Starting Flyway migrations..."

# Wait for MySQL to be ready
until mysql -h localhost -u root -p${MYSQL_ROOT_PASSWORD} -e "SELECT 1" >/dev/null 2>&1; do
    echo "Waiting for MySQL to be ready..."
    sleep 2
done

echo "MySQL is ready. Running migrations..."

# Run migrations in order
for migration_file in /docker-entrypoint-initdb.d/migrations/V*.sql; do
    if [ -f "$migration_file" ]; then
        echo "Running migration: $(basename $migration_file)"
        mysql -h localhost -u root -p${MYSQL_ROOT_PASSWORD} sample_db < "$migration_file"
        if [ $? -eq 0 ]; then
            echo "Successfully applied: $(basename $migration_file)"
        else
            echo "Failed to apply: $(basename $migration_file)"
            exit 1
        fi
    fi
done

# Create flyway_schema_history table to simulate Flyway behavior
mysql -h localhost -u root -p${MYSQL_ROOT_PASSWORD} sample_db << 'EOF'
CREATE TABLE IF NOT EXISTS flyway_schema_history (
    installed_rank INT NOT NULL,
    version VARCHAR(50),
    description VARCHAR(200) NOT NULL,
    type VARCHAR(20) NOT NULL,
    script VARCHAR(1000) NOT NULL,
    checksum INT,
    installed_by VARCHAR(100) NOT NULL,
    installed_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    execution_time INT NOT NULL,
    success TINYINT(1) NOT NULL,
    PRIMARY KEY (installed_rank),
    INDEX flyway_schema_history_s_idx (success)
);

INSERT IGNORE INTO flyway_schema_history 
(installed_rank, version, description, type, script, installed_by, execution_time, success) 
VALUES 
(1, '1', 'Create author records table', 'SQL', 'V1__Create_author_records_table.sql', 'root', 10, 1),
(2, '2', 'Create post records table', 'SQL', 'V2__Create_post_records_table.sql', 'root', 15, 1);
EOF

echo "Flyway migrations completed successfully!"