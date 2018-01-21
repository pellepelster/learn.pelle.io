TEMP_DIR = ENV.fetch('TEMP_DIR')

require_relative 'util'

ARGV.each do|filename|
  puts "inserting snippets into file '#{filename}'"

  lines = create_lines(filename)
  snippets = extract_snippets(lines)

  raw_lines = lines.collect {|line| line[:content] }

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

    puts " inserting snippet file '#{snippet_filename}' into #{filename}"

    snippet_lines = []
    File.readlines(snippet_filename).map do |line|
      snippet_line = {}
      snippet_line[:content] = line
      snippet_lines.push(snippet_line)
    end

    lines[snippet[:start]+1...snippet[:end]]=snippet_lines

  end

  content = lines.collect {|line| line[:content] }.join('')
  puts content
  File.write(filename, content)

end
