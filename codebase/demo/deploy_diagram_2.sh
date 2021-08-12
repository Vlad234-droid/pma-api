rm -f diagram_2.zip
zip diagram_2.zip diagram_2.bpmn

export SERVICE_HOME=$(pwd)

curl -d "$SERVICE_HOME/diagram_2.zip" -H "Content-Type: text/plain" -X POST http://localhost:8083/v1/processes
