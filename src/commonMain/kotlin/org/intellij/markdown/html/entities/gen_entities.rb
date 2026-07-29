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

object Entities {
EOF

json = JSON.parse(File.read("entities.json"))
entries = json.map { |key, value| [key, value['codepoints'][0]] }
chunks = entries.each_slice(256).to_a

puts "    val map: Map<String, Int> = #{(0...chunks.length).map { |i| "map#{i}()" }.join(' + ')}"

chunks.each_with_index do |chunk, index|
  puts
  puts "    private fun map#{index}(): Map<String, Int> = hashMapOf("
  chunk.each_with_index do |(key, codepoint), entry_index|
    puts ',' unless entry_index.zero?
    print "\"#{key}\" to #{codepoint}"
  end
  puts ")"
end

puts "}"

`rm entities.json`
$stderr.puts "Done"
