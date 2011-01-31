puts "Starting : Create PAR Project Structure (" + project_name + ")"

script_base = get_script_location()
spring_build_folder = File.join(script_base, "../../../spring-build")
par_build_template = File.join(script_base, "../../../tinos/par-build")
par_folder_template = File.join(script_base, "../../../tinos/par")

#
# Create the basic layout
# + project_name
#   - dist
#   - par-provided 
#   + projects
#		- spring-build
#		- build-<project_name>
#		- <organisation> (PAR Project)
#		- <Additional Bundles wrapped via PAR are here>
#
FileUtils.remove_dir(project_dir, true)
FileUtils.mkdir_p(project_dir)
FileUtils.mkdir_p(project_dir + '/dist')
FileUtils.mkdir_p(project_dir + '/par-provided')
FileUtils.mkdir_p(project_dir + '/projects')

# Create the Root readme.txt
file_template = File.join(script_base,
		"../../../tinos/par-misc/par-root-readme.txt")
create_file_from_template(file_template,
		project_dir + '/readme.txt', binding)

# Copy in the required Spring-Build to Projects
FileUtils.cp_r(spring_build_folder, project_dir + '/projects' + '/spring-build')

# Setup the Generic Par Build Options
t_path = project_dir + "/projects/build-#{project_name}"
FileUtils.mkdir_p(t_path)
Dir.foreach(par_build_template) do |entry|
	if include_entry?(entry)
		entry_path = File.join(par_build_template, entry)
		target_path = File.join(t_path, entry)
		if File.file?(entry_path)
			create_file_from_template(entry_path, target_path, binding)
		end
	end
end

# Setup the basic readme/build.properties/build.versions
file_template = File.join(script_base,
		"../../../tinos/par-misc/build.versions")
create_file_from_template(file_template,
		project_dir + '/projects/build.versions', binding)

file_template = File.join(script_base,
		"../../../tinos/par-misc/build.properties")
create_file_from_template(file_template,
		project_dir + '/projects/build.properties', binding)

file_template = File.join(script_base,
		"../../../tinos/par-misc/readme.txt")
create_file_from_template(file_template,
		project_dir + '/projects/readme.txt', binding)



# Setup the PAR Project Folder
par_project_path = project_dir + "/projects/#{organization}"
FileUtils.mkdir_p(par_project_path)

# Zap in the PAR project template
file_template = File.join(script_base,
		"../../../tinos/par-project")
make_dir(file_template, par_project_path, binding)

puts "Finished : Create PAR Project Structure (" + project_name + ")"
puts "\n\tNow that the PAR Structure is complete, you can add bundle"
puts "\twithin it. To create the individual bundles within this"
puts "\tstructure:"
puts "\t\tcreate -n BundleName -t " + project_dir + '/projects' + " -o " + organization + '.bundle_a' + " -a bundle\n"
