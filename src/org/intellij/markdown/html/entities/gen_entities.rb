#!/usr/bin/ruby

require 'json'

def die
    $stderr.puts "Error"
      exit
end

$stderr.puts "Checking out entities"
die unless `wget https://html.spec.whatwg.org/multipage/entities.json 2> /dev/null`

puts <<EOF
package org.intellij.markdown.html.entities

/**
 * This is generated file, DO NOT EDIT
  * Generated with gen_entities.rb
   */

public object Entities {
    public val map: Map<String, Int> = hashMapOf(
                
EOF

json = JSON.parse(File.read("entities.json"))
is_first = true
json.each do |key, value|
  puts ',' unless is_first
  is_first = false

  print "\"#{key}\" to #{value['codepoints'][0]}"
end

puts <<EOF
)
}
EOF

`rm entities.json`
$stderr.puts "Done"
