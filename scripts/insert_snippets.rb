TEMP_DIR = ENV.fetch('TEMP_DIR')
ARTEFACTS_DIR = ENV.fetch('ARTEFACTS_DIR')

require_relative 'util'

ARGV.each do|filename|
  puts "inserting snippets into file '#{filename}'"

  lines = create_lines(filename)
  snippets = extract_snippets(lines)

  snippets.reverse_each do |snippet_id,snippet|
    snippet_filename = "#{TEMP_DIR}/snippet_#{snippet_id}"

    if(!snippet[:start])
      puts "snippet '#{snippet_id}' has no start tag"
      next
    end

    if(!snippet[:end])
      puts "snippet '#{snippet_id}' has no end tag"
      next
    end

    puts "inserting snippet file '#{snippet_filename}' into #{filename}"
    snippet_lines = []
    File.readlines(snippet_filename).map do |line|
      snippet_line = {}
      snippet_line[:content] = line
      snippet_lines.push(snippet_line)
    end

    lines[snippet[:start]+1...snippet[:end]]=snippet_lines

  end

  content = lines.collect {|line| line[:content] }.join('')
  File.write(filename, content)

  puts "inserting files into file '#{filename}'"

  lines = create_lines(filename)
  files = extract_files(lines)

  files.reverse_each do |file_id,file|
    insert_filename = "#{ARTEFACTS_DIR}/#{file_id}"

    if(!file[:start])
      puts "file '#{file_id}' has no start tag"
      next
    end

    if(!file[:end])
      puts "file '#{file_id}' has no end tag"
      next
    end

    puts "inserting file '#{insert_filename}' into #{filename}"
    file_lines = []
    file_lines.push({ content: "{{% github href=\"#{insert_filename}\" %}}#{File.basename insert_filename}{{% /github %}}\n" })
    file_lines.push({ content: "{{< highlight go \"linenos=table,linenostart=,hl_lines=\" >}}\n" })

    File.readlines(insert_filename).map do |line|
      file_line = {}
      file_line[:content] = line
      file_lines.push(file_line)
    end
    file_lines.push({ content: "{{< / highlight >}}\n" })

    lines[file[:start]+1...file[:end]]=file_lines
  end

  content = lines.collect {|line| line[:content] }.join('')
  File.write(filename, content)

end
