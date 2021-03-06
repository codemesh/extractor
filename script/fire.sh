#!/bin/bash

help(){
    echo "usage: $0 <google result dir> <parallel root> [yyyymm]"
}

run_cmd(){
    echo "$@"
    $@
}

if [ $# -lt 2 ]; then
    help
    exit 1
fi

yyyymm=""
if [ $# -eq 3 ]; then
    yyyymm=$3
fi

categories="world china business science-technology culture-arts life-fashion travel education opinion"

google_result_dir=$1
parallel_root=$2
if [ "x$yyyymm" -eq "x" ];then
    yyyymm=$(date +%Y%m -d last-month)
fi
script_dir=$(dirname $0)

for category in $categories; do
    echo $category
    echo $yyyymm
    cmd="$script_dir/grab_category.sh $category $yyyymm $google_result_dir $parallel_root"
    run_cmd $cmd
    if [ $? -ne 0 ]; then
        echo "ERROR Failed to grab category $category of time $yyyymm"
        exit 1
    fi
    # try to happy google
    happy_google_seconds=$[$RANDOM % 60 + 30]
    
    cmd="sleep $happy_google_seconds"
    run_cmd $cmd
done
