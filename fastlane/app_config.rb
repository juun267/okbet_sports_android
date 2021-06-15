require 'fileutils'
require 'colorize'
require "./json_config.rb"


module URLMode
  FILE_PATH = "FILE_PATH"
  HTTP_LINK = "HTTP_LINK"
end

class AppConfig
	KEY_STORE = {
		"android.injected.signing.store.file" => "#{Dir.pwd}/../signKey/key3.jks",
        "android.injected.signing.store.password" => "desheng@2020",
        "android.injected.signing.key.alias" => "fungame",
        "android.injected.signing.key.password" => "desheng@2020"
    }

    def initialize(mode: mode, url: url, str_platforms: "")
    	@str_platforms = str_platforms
    	case mode
    	when URLMode::FILE_PATH
    		setup_json_file(url)
    	when URLMode::HTTP_LINK
    		setup_json_url(url)
    	end
    end

	# check if nothing mapped with platform data
	def zero_platform_mapped
		return mapping_platforms.count == 0
	end

	# split string by ',' to show input appid(s) not found
	def split_platform
		if @str_platforms == nil or @str_platforms == "" then
			return []
		end
		return @str_platforms.gsub(/\s+/, "").split(',')
	end

	# Turn platforms to arguments for gradle
	def mapped_platforms_properties
		infos = []
		mapping_platforms.each do |platform_info|
			properties = {}
			# add keystore info
			properties = properties.merge(KEY_STORE)
			properties = properties.merge({
				"fastlane_version_name" => dictionary_version,
				"fastlane_version_code" => short_version,
		        "fastlane_app_code" => platform_info["app_code"],
		        "fastlane_app_name" => platform_info["app_name"], 
		        "fastlane_application_id" => platform_info["appid"], 
		        "fastlane_jpush_appkey" => platform_info["jpush_appKey"],
		        "fastlane_jpush_channel" => "developer-default",
		        "fastlane_jpush_provider" => platform_info["appid"] + ".DataProvider",
		        "fastlane_jpush_permission" => platform_info["appid"] + ".permission.JPUSH_MESSAGE", 
		        "fastlane_security_config" => "@xml/test_network_security_config", 
		        "fastlane_res_src_dir" => "src/platform/#{platform_info["app_code"]}/res",
		        "fastlane_executor" => "fastlane"
			})

			infos.append(properties)
		end

		infos
	end


	private
	def setup_json_file(url)
		@config_json = JsonConfig.new(url)
	end

	def setup_json_url(url)
		@config_json = JsonConfig.new(url, isFile:false)
	end

	def json_platforms
		@config_json.platforms
	end

	# use input parameter str_platforms to map in json which data has same appid
	def mapping_platforms
		if @str_platforms == nil or @str_platforms == "" then
			return json_platforms 
		else
			split_platform.map { |str_platform| index_platform_dictionary(str_platform) }.select { |platform_info| platform_info != nil }
		end
	end

	def index_platform_dictionary(str_platform)
		for platform_info in json_platforms
			if str_platform == platform_info['app_code'] then
				return platform_info
			end
		end
		return nil
	end	

	def short_version
		@config_json.short_version
	end

	def dictionary_version
		@config_json.dictionary_version
	end

	def exec_bash_command(bash_command) 
		puts "#{Time.new.strftime("[%H:%M:%S]")} " + "bash $ ".blue + "#{bash_command}"
		system(bash_command)
	end
end