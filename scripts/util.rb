def has_snippet_start_marker(line)
  line.valid_encoding? && line.match?(/[^\/]snippet:(\S*)/)
end

def has_snippet_end_marker(line)
  line.valid_encoding? && line.match?(/\/snippet:(\S*)/)
end

def extract_snippet_id(line)
  if match = line.match(/\/?snippet:(\S*)/)
    match.captures[0]
  end
end

def create_lines(file)
  lines = []
  line_number = 1
  File.readlines(file).each do |line|
    line_info = {}
    line_info[:content] = line

    if (has_snippet_start_marker(line))
      line_info[:snippet_start] = true
      line_info[:snippet_id] = extract_snippet_id(line)
    elsif (has_snippet_end_marker(line))
      line_info[:snippet_end] = true
      line_info[:snippet_id] = extract_snippet_id(line)
    else
      line_info[:number] = line_number
      line_number += 1
    end

    lines.push(line_info)
  end

  lines
end

def extract_snippets(lines)
  snippets = {}

  lines.each_with_index do |line_info,index|
    if (line_info[:snippet_start])
      snippet = {}
      snippet[:start] = index
      snippets[line_info[:snippet_id]] = snippet
    end

    if (line_info[:snippet_end])
      if !snippets[line_info[:snippet_id]]
        puts "no snippet start found for '#{line_info[:snippet_id]}'"
      end
      p snippets[line_info[:snippet_id]]

      snippets[line_info[:snippet_id]][:end] = index
    end
  end

  snippets
end
