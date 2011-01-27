puts "Starting : Create Bundle Project Structure (" + organization + ")"

script_base = get_script_location()
bundle_template = File.join(script_base, "../../tinos/bundle-project")
bundle_org = organization + "." + project_name
target_folder = File.join(target_dir, "/" + bundle_org)
# Remove default java template "project_name"
FileUtils.remove_dir(project_dir, true)
# Wipe target folder if it already exists! (possibly a bad thing)
FileUtils.remove_dir(target_folder, true)
FileUtils.mkdir_p(target_folder)
make_dir(bundle_template, target_folder, binding)

# Interface Definition
#src_path = "/" + organization.gsub('.','/')
src_path = "/" + bundle_org.gsub('.','/')
src_folder = target_folder + '/src/main/java/' + src_path
FileUtils.mkdir_p(src_folder)
file_template = File.join(script_base,
	"../../tinos/bundle-misc/BundleService.java")
create_file_from_template(file_template,
		src_folder + '/BundleService.java', binding)

# Interface Implementation
impl_folder = src_folder + '/impl'
FileUtils.mkdir_p(impl_folder)
file_template = File.join(script_base,
	"../../tinos/bundle-misc/BundleServiceImpl.java")
create_file_from_template(file_template,
		impl_folder + '/BundleServiceImpl.java', binding)

# Test File
test_folder = target_folder + '/src/test/java/' + src_path
FileUtils.mkdir_p(test_folder)
file_template = File.join(script_base,
	"../../tinos/bundle-misc/BundleServiceImplTests.java")
create_file_from_template(file_template,
		test_folder + '/BundleServiceImplTests.java', binding)

puts "Finished : Create Bundle Project Structure (" + bundle_org + ")"
puts "\n\tNote:"
puts "\t\tDon't forget to Add this bundle to the <PLAN-FOLDER> Build File\n"
puts "\n\t\tTo generate the .classpath file for eclipse"
puts "\t\tcd " + target_folder + "; ant eclipse\n\n"


#test_organization = organization + "." + project_name + ".integration.test"
#puts "Starting : Create Integration Test Bundle Project Structure (" + test_organization + ")"
# Generate Test Bundle
#test_bundle_template = File.join(script_base, "../../tinos/bundle-integration-test-project")
#test_target_folder = File.join(target_dir, "/" + test_organization)
#FileUtils.remove_dir(test_target_folder, true)
#FileUtils.mkdir_p(test_target_folder)
#make_dir(test_bundle_template, test_target_folder, binding)

# Add tests
#src_path = "/" + organization.gsub('.','/') + "/integration/test"
#test_organization = organization + ".integration.test"
#test_folder = test_target_folder + '/src/test/java/' + src_path
#FileUtils.mkdir_p(test_folder)
#
#file_template = File.join(script_base,
#	"../../tinos/bundle-misc/boot-bundles.properties")
#create_file_from_template(file_template,
#		test_folder + '/boot-bundles.properties', binding)
#
#file_template = File.join(script_base,
#	"../../tinos/bundle-misc/IvyPackagedArtifactFinder.java")
#create_file_from_template(file_template,
#		test_folder + '/IvyPackagedArtifactFinder.java', binding)
#file_template = File.join(script_base,
#	"../../tinos/bundle-misc/LocalFileSystemIvyRepository.java")
#create_file_from_template(file_template,
#		test_folder + '/LocalFileSystemIvyRepository.java', binding)
#file_template = File.join(script_base,
#	"../../tinos/bundle-misc/SimpleBundleTest.java")
#create_file_from_template(file_template,
#		test_folder + '/SimpleBundleTest.java', binding)
#
#puts "Finished : Create Integration Test Bundle Project Structure (" + test_organization + ")"
#
#puts "\n\tNote:"
#puts "\n\t\tTo generate the .classpath file for eclipse"
#puts "\tcd " + test_target_folder + "; ant eclipse\n\n"

