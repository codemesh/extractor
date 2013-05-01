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
    exit 1
fi

categories="world china business science-technology culture-arts life-fashion travel education opinion"

google_result_dir=$1
parallel_root=$2
last_yyyymm=$(date +%Y%m -d last-month)
script_dir=$(dirname $0)

for category in $categories; do
    echo $category
    echo $last_yyyymm
    cmd="$script_dir/grab_category.sh $category $last_yyyymm $google_result_dir $parallel_root"
    run_cmd $cmd
    if [ $? -ne 0 ]; then
        echo "ERROR Failed to grab a category"
        exit 1
    fi
done
