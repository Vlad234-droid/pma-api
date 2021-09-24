#!/bin/sh

set -e
set -u

export Bearer="eyJraWQiOiIwNWU0ZjJkOS05MDY4LTQ5MGYtYTBmYy1jMTg4Y2NlODY5ZDciLCJhbGciOiJSUzI1NiJ9.eyJqdGkiOiI4OGMyMzRkMS1kZGUwLTQwMzYtYjU5YS1mNzRjNzllMWNmYjQiLCJpc3MiOiJodHRwczovL2FwaS1wcGUudGVzY28uY29tL2lkZW50aXR5L3Y0L2lzc3VlLXRva2VuIiwic3ViIjoiNzhmZTUxNDktZDAxMS00MjNkLWIyMTgtZGQzY2M5Njc1MDhjIiwiaWF0IjoxNjMyMzg4MTk4LCJuYmYiOjE2MzIzODgxOTgsImV4cCI6MTYzMjM5MTc5OCwic2NvcGUiOiJpbnRlcm5hbCBwdWJsaWMiLCJjb25maWRlbmNlX2xldmVsIjoxMiwiY2xpZW50X2lkIjoiNzhmZTUxNDktZDAxMS00MjNkLWIyMTgtZGQzY2M5Njc1MDhjIiwidG9rZW5fdHlwZSI6ImJlYXJlciJ9.OMLpzLk2Ohcsga_e16NO0V2m5Z-rCIfZfejnF_ktxNtfXwAEFCMHtZEo9E8FrI3DA4D1bF9j_ag7BUyzV9k29UFwQ7uUZwfgzkJBJWx2D0mw2IoyrZtaQuPsefVIR1uhklCBIxugwp52i_JaXlYBfRsM3Z8dYfsfRAjpdi0peLEveVXVkFsxyB-ZmOPUSg1xc3Vke9UfnsGYBUHfYc1-h7vKacHWq92o2_wVzyYJ2ox3uAdOwEVVq0UMbE5FCq4GKAGGxL1cxcWqFkBeaaK1W-qHsynHzbsj88xJ6OI-AVf8DV5FrCm4yDo4_u4JIucpQNe0wh2QX8IFlcA_NHlRhQ"
export PMA_SERVER="http://pma-api-service:8085"

for i in 'Joiner' 'Mover' 'Leaver' 'Reinstatement'
do
    for j in 'jit' 'immediate' 
    do
        echo "Submit an event for $i with delivery mode $j"
        curl -v -i -H "Accept: */*" -H "Content-Type: application/json" -H  "Authorization: Bearer $Bearer" -X POST $PMA_SERVER/v1/colleagues/events -d @/scripts/tests/cep/$j/$i.json
        sleep 5
    done
    sleep 10
done
