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
google_cookie="PREF=ID=6e67dbdb1e089cab:FF=0:LD=en:NW=1:CR=2:TM=1367592382:LM=1367592382:S=cEWqvo5pihSzZ4GC; NID=67=Unm8wN3BVMeGNaA2UJgRRp6EQgtmG1ephXNnCB46hthmncK8fFJiaDwTXHw23c_rN4mO61pd-_Ncbc8tO_r3vswj4cmy-_UPjmvBD-gYfgradSXSfbLRDzeEyPpr9t4Q"
google_referer="https://www.google.com/"
google_page="start=$start"
google_gsl="gs_l=serp.12...0.0.1.1234.0.0.0.0.0.0.0.0..0.0...0.0...1c..11.serp.hxk27iBioNg"

fetch_cmd="curl -s -S -x https://localhost:8087 -e '$google_referer' 'http://www.google.com/search?newwindow=1&biw=1366&bih=638&output=search&sclient=psy-ab&$google_query&$google_page' -b '$google_cookie' -A '$google_ua' -o $page_output"
# This is really gossip
run_cmd $fetch_cmd

