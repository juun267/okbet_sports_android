require 'json'
require 'fileutils'
require 'colorize'

class AppConfig
	KEY_STORE = {
		"android.injected.signing.store.file" => "#{Dir.pwd}/../signKey/key3.jks",
        "android.injected.signing.store.password" => "desheng@2020",
        "android.injected.signing.key.alias" => "fungame",
        "android.injected.signing.key.password" => "desheng@2020"
    }

	def initialize
		@config_info = JSON.parse(File.read('./config.json'))
	end

	def zero_platform_mapped(str_platforms = nil)
		return mapping_platforms(str_platforms).count == 0
	end

	def split_platform(str_platforms = nil)
		if str_platforms == nil or str_platforms == "" then
			return []
		end
		return str_platforms.gsub(/\s+/, "").split(',')
	end

	def mapping_platforms(str_platforms = nil)
		if str_platforms == nil or str_platforms == "" then
			return @config_info['platform']
		else
			split_platform(str_platforms).map { |str_platform| map_platform(str_platform) }.select { |platform_info| platform_info != nil }
		end
	end

	def short_version
		@config_info['short_version']
	end

	def dictionary_version
		@config_info['dictionary_version']
	end


	private
	def get_platforms
		@config_info['platform']
	end

	def map_platform(str_platform)
		for platform_info in get_platforms
			if str_platform == platform_info['app_code'] then
				return platform_info
			end
		end
		return nil
	end

	def exec_bash_command(bash_command) 
		puts "#{Time.new.strftime("[%H:%M:%S]")} " + "bash $ ".blue + "#{bash_command}"
		system(bash_command)
	end
end