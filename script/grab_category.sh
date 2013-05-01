#!/bin/bash
help(){
    echo "usage: $0 <category> <yyyymm> <google result dir> <parallel_root>"
}

run_cmd(){
    echo "$@"
    eval $@
}

if [ $# -lt 4 ]; then
    help
    exit 1
fi


category=$1
yyyymm=$2
top_output_dir=$3
parallel_root=$4
script_dir=$(dirname $0)
yyyy=$(echo $yyyymm | grep -oP '^[0-9]{4}')
mm=$(echo $yyyymm | grep -oP '[0-9]{2}$')
month_abbr=$(date +%b -d $yyyymm"01")
for page in `seq 1 100`; do
    category_output_dir="$top_output_dir/$category""_p$page""_$yyyy$month_abbr/"
    cmd="mkdir -p $category_output_dir"
    run_cmd $cmd
    category_output_file=$category_output_dir/$category"_$yyyy""_$mm""_p$page.html"
    
    cmd="$script_dir/fetch_google_result.sh $category $yyyymm $page $category_output_file"
    run_cmd $cmd
    cmd="$script_dir/run.sh $category_output_dir $parallel_root"
    run_cmd $cmd
    if [ $? -ne 0 ]; then
        echo "ERROR Failed to grab category $category"
        exit 1
    fi
    # To test whether this is the last google result page
    grep -oP 'class="cur".+class="b navend"' $category_output_file | grep -o 'class="fl"' >& /dev/null
    
    if [ $? -ne 0 ]; then
        break
    fi
done

    
