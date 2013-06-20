#!/bin/bash
help(){
    echo -e "usage: $0 <yyyymm> [<category>]\nTokenizes ./split/<yyyymm>/* into ./tokenized/<yyyymm>/<same path as before>"
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

segmenter_dir="/home/fengyu/dev/nlp/stanford-segmenter-2013-04-04/"
category=""
if [ $# -eq 2 ]; then
    category=$2
fi

source_top="split"
target_top="tokenized"

if [ ! -d $source_top ]; then
    echo "source directory 'split' not exist"
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

source_dir="$yyyyMmm"

find_dir_cmd="find $source_dir -maxdepth 1 -mindepth 1 -type d"

category_filter='cat'
if [ x"$category" != x"" ]; then
    # Why this does not work?
#    find_dir_cmd="$find_dir_cmd -name '$category*'"
#    category_filter=" -name '$category*'"
    category_filter="grep $source_dir/$category"
fi

tmp_file_list=$(mktemp)
cd $source_top
dir_list=$($find_dir_cmd | $category_filter)

for dir in $dir_list; do
    source_dir="$dir/cn"
    find_cmd="find $source_dir -maxdepth 1 -mindepth 1 -type f"
    echo "find_cmd is: $find_cmd"
    $find_cmd >> $tmp_file_list
done

cd -
tokenize_cmd="$segmenter_dir/batch_segment.sh ctb $tmp_file_list $source_top $target_top"
echo "tokenize_cmd is: $tokenize_cmd"
$tokenize_cmd
rm $tmp_file_list
