/*
 * 2010 (c) Pouzin Society
 *
 * Author        : pphelan(at)tssg.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package sample.snap;

import java.util.List;
import sample.api.SampleDao;
import sample.api.SampleAPI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Annotation-driven <em>MultiActionController</em> that handles all non-form
 * URL's.
 *
 * Used to provide sample snap for OpenTinos Console.
 */
@Controller
public class SampleController {
	private final SampleAPI apiService;
	private static Log log = LogFactory.getLog(SampleController.class);

	@Autowired
	public SampleController(SampleAPI apiService) {
		this.apiService = apiService;
	}

	/**
	 * Custom handler for displaying index 
	 * @return a ModelMap with the model attributes for the view
	 */
	@RequestMapping("/sample/index")
	public ModelMap indexHandler() {
		List<SampleDao> daoList;
		log.info("Calling OSGi Service");
		daoList = apiService.get();
		log.info("Returned from OSGi Service");
		return new ModelMap().addAttribute("daoList", daoList);
	}
	
	/**
	 * Custom handler for displaying index 
	 * @return a ModelMap with the model attributes for the view
	 */
	@RequestMapping("/sample/delete")
	public String deleteHandler(@RequestParam("textVal") String text) {
		log.info("Calling OSGi Service");
		apiService.remove(new SampleDao(text));
		log.info("Returned from OSGi Service");
		return "redirect:/sample/index.htm";
	}
}
