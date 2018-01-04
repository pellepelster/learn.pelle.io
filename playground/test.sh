#!/bin/bash

TEMP_DIR=./tmp
if [[ ! -d ${TEMP_DIR} ]]; then
  mkdir ${TEMP_DIR}
fi

function generate_snippets() {
  (
    TEMPLATE_FILE=${1:-}
    echo "generating snippets for ${TEMPLATE_FILE}"
    local template_filename=$(basename ${TEMPLATE_FILE})
    # number lines
    nl -ba -s ': ' -w 1 ${TEMPLATE_FILE} > ${TEMP_DIR}/${template_filename}.numbered

    # add split marker and split files
    cat ${TEMP_DIR}/${template_filename}.numbered | sed 's/.*\/\/snippet:.*/---split---\n&/' > ${TEMP_DIR}/${template_filename}.numbered.with_split_markers
    csplit ${TEMP_DIR}/${template_filename}.numbered.with_split_markers -f ${TEMP_DIR}/${template_filename}.splitted /---split---/ '{*}'

    for FILE in ${TEMP_DIR}/${template_filename}.splitted*; do
      # get snippet filename from //snippet:$name tag
      TMP=$(grep -oP "snippet:(.*)" ${FILE})
      SNIPPET_ID=${TMP#snippet:}
      if [ ! -z "${SNIPPET_ID}" ]; then
        echo "processing snippet ${SNIPPET_ID}"
        # reduce snippets to actual snippet content
        awk '/snippet/{flag=1} /eos/{print;flag=0} flag' ${FILE} > ${TEMP_DIR}/"snippet_${SNIPPET_ID}"
      fi
    done
  )
}

function insert_snippets() {
  (
    local snippet_file=${1:-}
    local content_dir=${2:-}

    echo "inserting snippets for ${snippet_file} in content dir ${content_dir}"
    local snippet_id=$(basename ${snippet_file})
    snippet_id=${snippet_id#snippet_}
    while IFS= read -d $'\0' -r content_file ; do
      if [[ -f ${content_file} ]]; then
        echo "processing content file ${content_file}"
        local starttag="<!--snippet:${snippet_id}-->"
        local endtag="<!--eos:${snippet_id}-->"

        awk "
            BEGIN       {p=1}
            /^${starttag}/   {print;system(\"cat ${snippet_file}\");p=0}
            /^${endtag}/     {p=1}
            p" ${content_file} > ${TEMP_DIR}/content_${snippet_id}.tmp
            cp ${TEMP_DIR}/content_${snippet_id}.tmp ${content_file}

      fi
    done < <(find ${content_dir} -iname '*' -print0)
  )
}

function generate_and_insert_snippets() {
  local sources_dir=${1:-}
  local content_dir=${2:-}

  echo "generating snippets four source dir  ${sources_dir}"

  while IFS= read -d $'\0' -r source_file ; do
    generate_snippets ${source_file}
    for snippet_file in ${TEMP_DIR}/snippet_*; do
      insert_snippets ${snippet_file} ${content_dir}
    done
  done < <(find ${sources_dir} -iname '*' -type f -print0)

}

generate_and_insert_snippets ./sources ./content
