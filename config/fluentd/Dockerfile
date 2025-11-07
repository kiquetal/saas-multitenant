FROM fluent/fluentd:v1.16-1

# Use root account to use apk
USER root

# Install plugins
RUN apk add --no-cache --update --virtual .build-deps \
        build-base ruby-dev \
 && gem sources --clear-all \
 # --- THIS IS THE FIX ---
 # 1. Force-uninstall the OLD, conflicting plugin from the base image.
 && gem uninstall -x fluent-plugin-loki \
 # 2. Now, install the NEW, official plugin.
 && gem install fluent-plugin-grafana-loki --no-document \
 # ---
 && apk del .build-deps \
 && rm -rf /tmp/* /var/tmp/* /usr/lib/ruby/gems/*/cache/*.gem

COPY ./conf/fluent.conf /fluentd/etc/
USER fluent
USER fluent
