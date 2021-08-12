rm -f diagram_1.zip
zip diagram_1.zip diagram_1.bpmn

export SERVICE_HOME=$(pwd)

curl -d "$SERVICE_HOME/diagram_1.zip" -H "Content-Type: text/plain" -X POST http://localhost:8083/v1/processes
