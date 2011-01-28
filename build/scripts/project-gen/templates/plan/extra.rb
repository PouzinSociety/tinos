puts "Starting : Create PLAN Project Structure (" + organization + "." + project_name + ")"

script_base = get_script_location()
plan_template = File.join(script_base, "../../tinos/plan-project")
plan_name = organization + "." + project_name + ".plan"
target_folder = File.join(target_dir, "/" + plan_name)
#
# Remove default java template "project_name"
FileUtils.remove_dir(project_dir, true)
#
# Wipe target folder if it already exists! (possibly a bad thing)
FileUtils.remove_dir(target_folder, true)
FileUtils.mkdir_p(target_folder)
make_dir(plan_template, target_folder, binding)
#
# Add src folder
src_folder = target_folder + '/src'
FileUtils.mkdir_p(src_folder)
#
# Plan file
file_template = File.join(script_base,
		"../../tinos/plan-misc/deployment.plan")
create_file_from_template(file_template,
		src_folder + "/#{plan_name}", binding)

puts "Finished : Create PLAN Project Structure (" + organization + "." + project_name + ")"
