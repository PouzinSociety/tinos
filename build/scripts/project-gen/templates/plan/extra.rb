puts "Starting : Create PLAN Project Structure (" + project_name + ")"

script_base = get_script_location()
spring_build_folder = File.join(script_base, "../../spring-build")
plan_build_template = File.join(script_base, "../../tinos/plan-build")
plan_folder_template = File.join(script_base, "../../tinos/plan")

#
# Create the basic layout
# + project_name
#   - dist
#   - repository
#   + projects
#		- spring-build
#		- build-<project_name>
#		- <organisation> (Plan Project)
#		- <Additional Bundles wrapped via Plan/Build are here>
#
FileUtils.remove_dir(project_dir, true)
FileUtils.mkdir_p(project_dir)
FileUtils.mkdir_p(project_dir + '/dist')
FileUtils.mkdir_p(project_dir + '/repository')
FileUtils.mkdir_p(project_dir + '/src')
FileUtils.mkdir_p(project_dir + '/docs')
FileUtils.mkdir_p(project_dir + '/projects')

# Create the Root readme.txt
file_template = File.join(script_base,
		"../../tinos/plan-misc/plan-root-readme.txt")
create_file_from_template(file_template,
		project_dir + '/readme.txt', binding)

# Copy in the required Spring-Build to Projects
FileUtils.cp_r(spring_build_folder, project_dir + '/projects' + '/spring-build')

# Setup the Generic Plan Build Options
t_path = project_dir + "/projects/build-#{organization}"
FileUtils.mkdir_p(t_path)
Dir.foreach(plan_build_template) do |entry|
	if include_entry?(entry)
		entry_path = File.join(plan_build_template, entry)
		target_path = File.join(t_path, entry)
		if File.file?(entry_path)
			create_file_from_template(entry_path, target_path, binding)
		end
	end
end

# Setup the basic readme/build.properties/build.versions/IvySettings
file_template = File.join(script_base,
		"../../tinos/plan-misc/build.versions")
create_file_from_template(file_template,
		project_dir + '/projects/build.versions', binding)

file_template = File.join(script_base,
		"../../tinos/plan-misc/build.properties")
create_file_from_template(file_template,
		project_dir + '/projects/build.properties', binding)

# Ivy Settings
file_template = File.join(script_base,
		"../../tinos/plan-misc/ivysettings.xml")
create_file_from_template(file_template,
		project_dir + '/projects/ivysettings.xml', binding)


file_template = File.join(script_base,
		"../../tinos/plan-misc/readme.txt")
create_file_from_template(file_template,
		project_dir + '/projects/readme.txt', binding)



# Setup the Plan Project Folder
plan_project_path = project_dir + "/projects/#{organization}.plan"
FileUtils.mkdir_p(plan_project_path)

# Zap in the PAR project template
file_template = File.join(script_base,
		"../../tinos/plan-project")
make_dir(file_template, plan_project_path, binding)
file_template = File.join(script_base,
		"../../tinos/plan-misc/deployment.plan")
create_file_from_template(file_template,
		project_dir + "/projects/#{organization}.plan/src/#{organization}.plan", binding)

puts "Finished : Create Plan Project Structure (" + project_name + ")"
puts "\n\tNow that the Plan Structure is complete, you can add bundle"
puts "\twithin it. To create the individual bundles within this"
puts "\tstructure:"
puts "\t\tcreate -n BundleName -t " + project_dir + '/projects' + " -o " + organization + '.bundle_a' + " -a bundle\n"
