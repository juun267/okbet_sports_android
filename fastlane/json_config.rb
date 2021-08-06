require "json"
require "net/http"
require "uri"

class JsonConfig
	
	def initialize(url, isFile: true)
		if isFile then
			initialize_with_file(url)
		else
			initialize_with_uri(url)
		end
		
	end

	def short_version
		@config_info['short_version']
	end

	def dictionary_version
		@config_info['dictionary_version']
	end

	def platforms
		@config_info['platform']
	end


	private 
		def initialize_with_file(file_path)
			# treat Errno::ENOENT as JSON::ParserError
			raise JSON::ParserError.new "No such file or directory" if Dir.glob(file_path).first == nil
			@config_info = JSON.parse(File.read(file_path))
		end

		def initialize_with_uri(url)
			uri = URI.parse(url)
			http = Net::HTTP.new(uri.host, uri.port)
			request = Net::HTTP::Get.new(uri.request_uri)
			response = http.request(request)

			if response.code == "200" then
				@config_info = JSON.parse(response.body)
			else
				raise JSON::ParserError.new "Parse URI Error"
			end
		end
end