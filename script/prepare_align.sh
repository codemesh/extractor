#!/bin/bash
help(){
    echo -e "usage: $0 <yyyymm> [category]\nPrepare doc pairs for alignment"
}

# link files to the Gargantua dir structure
# param 1 is the dir whose files are to be linked
# param 2 is language type, cn or en
# param 3 is the destination Gargantua dir
#
link_files() {
    dir=$1
    language=$2
    dest_dir=$3
    dir_name=$(echo $dir | sed 's/\/\+/$/g')
    source_dir="$dir/$language"
    find_cmd="find $source_dir -maxdepth 1 -mindepth 1 -type f"
    echo "find_cmd is: $find_cmd"
    for file in `$find_cmd`; do
        file_name=$(basename $file)
        ln -s $file $gargantua_dir/$dest_dir/$file_name.txt
    done
    
}
    
if [ $# -lt 1 ]; then
    help
    exit 1
fi

yyyymm=$1
gargantua_dir="/home/fengyu/dev/nlp/sentence-align/Gargantua1.0c/corpus_to_align"
basedir=$(realpath $0)
basedir=$(dirname $basedir)

category=""
if [ $# -eq 2 ]; then
    category=$2
fi

split_dir="$basedir/split"
tokenized_dir="$basedir/tokenized"

yyyy=$(echo $yyyymm | grep -oP '^[0-9]{4}')
mm=$(echo $yyyymm | grep -oP '[0-9]{2}$')
month_abbr=$(date +%b -d $yyyymm"01")
yyyyMmm="$yyyy$month_abbr"

source_dir="$split_dir/$yyyyMmm"

find_dir_cmd="find $source_dir -maxdepth 1 -mindepth 1 -type d"
if [ x"$category" != x"" ]; then
    find_dir_cmd="$find_dir_cmd -path '$source_dir/$category*'"
fi

dir_list=$(eval $find_dir_cmd)

for dir in $dir_list; do
    link_files $dir en source_language_corpus_untokenized
    link_files $dir cn target_language_corpus_untokenized
done

source_dir="$tokenized_dir/$yyyyMmm"

find_dir_cmd="find $source_dir -maxdepth 1 -mindepth 1 -type d"
if [ x"$category" != x"" ]; then
    find_dir_cmd="$find_dir_cmd -path '$source_dir/$category*'"
fi

dir_list=$(eval $find_dir_cmd)
for dir in $dir_list; do
    link_files $dir en source_language_corpus_tokenized
    link_files $dir cn target_language_corpus_tokenized
done
