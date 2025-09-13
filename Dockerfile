FROM axelor/app-builder:7.2 AS builder

ARG JAVA_OPTS="-Xmx4g"
ARG APP_SOURCE="/app/open-platform-demo"
ARG DEBIAN_FRONTEND="noninteractive"
ARG MODULE_NAME="axelor-message"

COPY . /${MODULE_NAME}
WORKDIR /
RUN if [ ! -f ${MODULE_NAME}/app.zip ]; then echo "app.zip webapp archive not found. Existing."; exit 1; fi
RUN mv ${MODULE_NAME}/app.zip .
RUN unzip app.zip
RUN mv ${MODULE_NAME} ${APP_SOURCE}/modules
WORKDIR ${APP_SOURCE}

ARG NEXUS_READER_USERNAME
ARG NEXUS_READER_PASSWORD


RUN chmod +x gradlew && \
./gradlew --no-daemon clean assemble -PaxelorMavenUsername=${NEXUS_READER_USERNAME} -PaxelorMavenPassword=${NEXUS_READER_PASSWORD}

RUN mkdir -p ${APP_SOURCE}/webapps/ROOT && \
    unzip -q -o ${APP_SOURCE}/build/libs/*.war -d ${APP_SOURCE}/webapps/ROOT/


# Image to run tomcat with axelor-app
FROM tomcat:9.0-jre11-temurin

ARG MODULE_NAME="axelor-message"
ARG PROJECT_VENDOR="Axelor"
ARG CATALINA_MEMORY_ALLOC_PCT=90

ARG BUILD_DATE

# Refresh repositories and install tools needed by the entrypoint
RUN apt update && \
    apt install -y coreutils curl postgresql-client && \
    rm -rf /var/lib/apt/lists/*


# Copy app
COPY --from=builder /app/open-platform-demo/webapps ${CATALINA_HOME}/webapps

# Expose ports
EXPOSE 8080

# Copy entrypoint
COPY docker-entrypoint.sh /bin
RUN chmod +x /bin/docker-entrypoint.sh

# Label images
LABEL \
	maintainer="Axelor <support@axelor.com>" \
	org.label-schema.schema-version="1.0" \
	org.label-schema.build-date="${BUILD_DATE}" \
	org.label-schema.name="${MODULE_NAME}" \
	org.label-schema.vendor="${PROJECT_VENDOR}"

# Entrypoint
ENTRYPOINT ["/bin/docker-entrypoint.sh"]
CMD ["start"]
