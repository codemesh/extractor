#!/bin/bash
help(){
    echo -e "usage: $0 <yyyymm> [<category>]\nSplits ./parsed/<yyyymm>/* into ./split/<yyyymm>/<same path as before>"
}

run_cmd(){
    echo "$@"
    eval $@
}

if [ $# -lt 1 ]; then
    help
    exit 1
fi
yyyymm=$1


category=""
if [ $# -eq 2 ]; then
    category=$2
fi

source_top="parsed"
target_top="split"

if [ ! -d $source_top ]; then
    echo "source directory 'parsed' not exist"
    exit 1
fi

if [ ! -d $target_top ]; then
    mkdir $target_top
    if [ ! $? ]; then
        echo "Failed to create target directory '$target_top'"
        exit 1
    fi
fi

yyyy=$(echo $yyyymm | grep -oP '^[0-9]{4}')
mm=$(echo $yyyymm | grep -oP '[0-9]{2}$')
month_abbr=$(date +%b -d $yyyymm"01")
yyyyMmm="$yyyy$month_abbr"

source_dir="$source_top/$yyyyMmm"

find_dir_cmd="find $source_dir -maxdepth 1 -mindepth 1 -type d"

category_filter='cat'
if [ x"$category" != x"" ]; then
    # Why this does not work?
#    find_dir_cmd="$find_dir_cmd -name '$category*'"
#    category_filter=" -name '$category*'"
    category_filter="grep $source_dir/$category"
fi

dir_list=$($find_dir_cmd | $category_filter)

for dir in $dir_list; do
    source_dir="$dir/cn"
    find_cmd="find $source_dir -maxdepth 1 -mindepth 1 -type f"
    echo "find_cmd is: $find_cmd"
    for file in $($find_cmd); do
        echo "split $file"
        output_file=$(echo $file | sed s/^$source_top/$target_top/)
        output_dir=$(dirname $file | sed s/^$source_top/$target_top/)
        if [ ! -d $output_dir ]; then
            mkdir -p $output_dir
            if [ ! $? ]; then
                echo "Failed to make output dir: $output_dir"
                exit 1
            fi
        fi
        cat $file | perl split-sentences.cn.perl -q -l en -q > $output_file
    done
done

