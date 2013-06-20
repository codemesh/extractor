#!/bin/bash

help(){
    echo "usage: $0 <google result dir> <parallel root>"
}

run_cmd(){
    echo "$@"
    $@
}

if [ $# -lt 2 ]; then
    help
    exit 1;
fi
root="/home/fengyu/dev/workspace/extractor"
flow_package="com.tranplex.flow"
just_now=$(date +%F%H%M)
google_result_dir=$1
parallel_root=$2
fetch_name=$(basename $google_result_dir)
category=$(echo "$fetch_name" | grep -oP '^[-_a-zA-Z]+')
yearmm=$(echo "$fetch_name" | grep -oP '[0-9]+[a-zA-Z_]+$')
if [ "$category" == "" ]; then
    category="unknown"
fi
google_results=$(find $google_result_dir -maxdepth 1 -type f | awk '{printf $1" "}')

# list url
url_list="urilist_$just_now"
cmd="touch $url_list"
run_cmd $cmd
cmd="$root/script/runconftool.sh $flow_package.UrlLister -o $url_list $google_results"
run_cmd $cmd
if [ $? -ne 0 ]; then
    echo "ERROR Failed to list urls from google result, google may have blocked us."
    exit 1;
fi

# fetch
fetched_dir=fetched_$just_now
cmd="mkdir $fetched_dir"
run_cmd $cmd
#cmd="$root/script/runtool.sh $flow_package.PageFetcher -o $fetched_dir $url_list"
cmd="$root/script/runconftool.sh $flow_package.PageFetcher -o $fetched_dir $url_list"
run_cmd $cmd

# parse
parallel_category="$parallel_root/$yearmm/$fetch_name"
if [ ! -d $parallel_category ]; then
    cmd="mkdir -p $parallel_category"
    run_cmd $cmd
fi
cmd="$root/script/runconftool.sh $flow_package.NYTimesExtractor -o $parallel_category $fetched_dir"
run_cmd $cmd

#clean
this_month=$(echo "$fetch_name" | grep -oP '[0-9]+[a-zA-Z]+$')
if [ "$this_month" == "" ]; then
    this_month=$(date +%Y%b)
fi

# archive url list
if [ ! -d stat ]; then
    cmd="mkdir stat"
    run_cmd $cmd
fi
url_list_archive="stat/urllist_$this_month"
cmd="touch $url_list_archive"
run_cmd $cmd
cmd="cat $url_list >> $url_list_archive"
echo $cmd
eval $cmd

cmd="rm $url_list"
run_cmd $cmd

# update fetch history
fetch_history="stat/fetch_history"
cmd="touch $fetch_history"
run_cmd $cmd
cmd="echo $fetch_name >> $fetch_history"
echo $cmd
eval $cmd

# delete google result page files
cmd="rm -r $google_result_dir/*_files/"
run_cmd $cmd

# move fetched pages
cmd="mv $fetched_dir $google_result_dir"
run_cmd $cmd
