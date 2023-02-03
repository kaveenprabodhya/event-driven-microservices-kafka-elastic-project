FROM alpine

RUN apt-get update && apt-get install -y curl && apt-get install -y kafkacat

#HEALTHCHECK --interval=30s --timeout=5s \
#  CMD curl -fs http://rest-proxy:8082/topics/twitter-topic || exit 1

#CMD sh -c "until curl -s -o /dev/null -w '%{http_code}' http://rest-proxy:8082/topics/twitter-topic | grep 200 > /dev/null; do sleep 30; done; echo 'Topic is available.'"

#CMD while true; do \
#    STATUS=$(curl -s -o /dev/null -w %{http_code} http://rest-proxy:8082/topics/twitter-topic); \
#    if [ "$STATUS" -eq 200 ]; then \
#        echo "Topic is available."; \
#        exit 0; \
#    fi; \
#    sleep 30; \
#done