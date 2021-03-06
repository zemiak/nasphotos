#!/bin/sh

baseFolder=/data/

main() {
    cd "${baseFolder}"
    find . -maxdepth 1 -type d | while read mfile
    do
        echo "${mfile}" | grep -E '.{0}[[:digit:]]{4}$' >/dev/null
        if [ $? -eq 0 ]
        then
            yearFolder "${mfile}"
        fi
    done
}

yearFolder()
{
    echo "... year folder ${1}"
    cd "${1}"
    find . -maxdepth 1 -type d | while read yfile
    do
        echo "${yfile}" | grep -E '.{0}[[:digit:]]{4}.+$' >/dev/null
        if [ $? -eq 0 ]
        then
            pictureFolder "${yfile}"
        fi
    done
    cd ..
}

pictureFolder() {
    cd "${1}"
    find . -maxdepth 1 -type f | while read photoFile
    do
        echo "${photoFile}" | grep -Ei '.*\.(mov|mp4)$' >/dev/null
        if [ $? -eq 0 ]
        then
            filename=$(basename -- "$photoFile")
            extension="${filename##*.}"
            filename="${filename%.*}"
            if [ -f "./${filename}.jpg" -o -f "./${filename}.JPG" -o -f "./${filename}.png" -o -f "./${filename}.PNG" ]
            then
                echo pass ${photoFile} >/dev/null
            else
                createThumbnail "${photoFile}" "${filename}.jpg"
            fi
        fi
    done
    cd ..
}

createThumbnail() {
    mkdir -p video-thumbnails
    ffmpeg -i "${1}" -vframes 1 -an -s 720x399 -ss 30 "./video-thumbnails/${2}"
}

saveFolder="$(pwd)"
main
cd "${saveFolder}"
