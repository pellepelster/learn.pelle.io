TEMP_DIR = ENV.fetch('TEMP_DIR')

require_relative 'util'

ARGV.each do|filename|
  puts "inserting snippets into file '#{filename}'"

  lines = create_lines(filename)
  snippets = extract_snippets(lines)

  raw_lines = lines.collect {|line| line[:content] }

  snippets.each do |snippet_id,snippet|
    snippet_filename = "#{TEMP_DIR}/snippet_#{snippet_id}"
    # puts " snippet file '#{snippet_filename}'"

    if(!snippet[:start])
      puts "snippet '#{snippet_id}' has no start tag"
      exit
    end

    if(!snippet[:end])
      puts "snippet '#{snippet_id}' has no end tag"
      exit
    end

    start = snippet[:start]
    length = snippet[:end] - snippet[:start] + 1

p snippet
    # puts start
    # puts length
    #
    # p raw_lines[start...length]

  end

end
