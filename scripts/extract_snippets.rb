TEMP_DIR = ENV.fetch('TEMP_DIR')

require_relative 'util'

ARGV.each do|filename|
  puts "extracting snippets from file '#{filename}'"

  lines = create_lines(filename)
  snippets = extract_snippets(lines)

  snippets.each do |snippet_id,snippet|
    snippet_filename = "#{TEMP_DIR}/snippet_#{snippet_id}"
    puts "writing snippet file '#{snippet_filename}'"

    if(!snippet[:start])
      puts "snippet '#{snippet_id}' has no start tag"
      exit
    end

    if(!snippet[:end])
      puts "snippet '#{snippet_id}' has no end tag"
      exit
    end

    start = snippet[:start] + 1
    length = snippet[:end] - snippet[:start] - 1
    indented = false
    indention = ""
    if (lines[start][:indented])
      indented = true
      indention = lines[start][:indention]
    end

    snippet_content = lines[start, length].collect {|line| line[:content] }.join('')

    open(snippet_filename, 'w') { |file|
      line_start = lines[snippet[:start] + 1][:number]
      line_end = lines[snippet[:end] -1][:number]
      file.puts "{{% github href=\"#{filename}#L#{line_start}-L#{line_end}\" %}}#{File.basename filename}{{% /github %}}"
      file.puts "{{< highlight go \"linenos=table,linenostart=#{line_start},hl_lines=\" >}}"
      if (indented)
        file.puts lines[0, start].reverse.select { |x| !x[:indented] && !x[:empty] }.first[:content]
        file.puts "\n#{indention}[..]\n\n"
      end
      file.puts snippet_content
      if (indented)
        file.puts "\n#{indention}[..]\n\n"
        file.puts lines[start, lines.length].select { |x| !x[:indented] && !x[:empty] }.first[:content]
      end
      file.puts '{{< / highlight >}}'
    }
  end
end
