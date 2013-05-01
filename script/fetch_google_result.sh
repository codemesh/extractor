#!/bin/bash

help(){
    echo "usage: $0 <category> <yyyymm> <page no> <page_output>"
}

run_cmd(){
    echo "$@"
    eval $@
}

if [ $# -lt 4 ]; then
    help
    exit 1;
fi

category=$1
yyyymm=$2
page_no=$3
page_output=$4
yyyy=$(echo $yyyymm | grep -oP '^[0-9]{4}')
mm=$(echo $yyyymm | grep -oP '[0-9]{2}$')
start=$[($page_no - 1)  * 10]
google_query="q=site%3Acn.nytimes.com%2Farticle%2F$category%2F$yyyy%2F$mm+inurl%3Adual"
google_page="start=$start"
google_cookie="NID=67=KBQb_ho3B25wlamCOx-GyQpU1a7Y9pK9DfNJCzvd7nHxVKI-45c6lCwLBJ7q9F8XOxTVEk_2meMsLLc94QLlfCm3hp64g4jzsFCss3ipkAoJzYU5ypPugva74kcKpJL3; PREF=ID=db941bc3bbe6d594:U=40af338b4159384b:FF=0:LD=en:NW=1:CR=2:TM=1367401194:LM=1367402789:S=r_X6ezqXu-EqCwCd; GDSESS=ID=90b4305f8bc46a9b:TM=1367402872:C=c:IP=125.39.34.208-:S=APGng0sQgJ8cFiIKC5Jalmzm48tkwV5COQ"
google_ua="Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.17 (KHTML, like Gecko) Chrome/24.0.1312.45 Safari/537.17"

fetch_cmd="curl -s -S -x https://localhost:8087 'http://www.google.com/search?newwindow=1&biw=1366&bih=638&start=10&output=search&sclient=psy-ab&$google_page&$google_query&btnK=' -b '$google_cookie' -A '$google_ua' -o $page_output"
run_cmd $fetch_cmd
