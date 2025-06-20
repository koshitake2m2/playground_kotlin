echo "###### MYSQL INIT BEGIN ###################################"

my_mysql_config () {
  cat <<-EOF
    [client]
    password=password
EOF
}

sql_files="`ls /docker-entrypoint-initdb.d/sql/*.sql`"

error_sql_files=""
for file in $sql_files; do
  echo "`date --rfc-3339=seconds` [MYSQL Note]: running $file"
  mysql --defaults-extra-file=<(my_mysql_config) -h localhost -u root < $file;
  if [ $? -eq 1 ]; then
    error_sql_files="$error_sql_files $file"
  fi
  echo
done

if [ -n "$error_sql_files" ]; then
  echo "`date --rfc-3339=seconds` [MYSQL Note]: MySQL init process failed."
  for file in $error_sql_files; do
    echo "`date --rfc-3339=seconds` [MYSQL ERROR Note]: error in $file"
  done
  exit 1
fi

echo "###### MYSQL INIT END #####################################"
