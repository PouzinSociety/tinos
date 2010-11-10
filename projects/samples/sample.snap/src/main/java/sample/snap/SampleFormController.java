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

import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sample.api.SampleDao;
import sample.api.SampleAPI;

@Controller
@RequestMapping("/sample/addDaoForm")
@SessionAttributes("dao")
public class SampleFormController {
	private final SampleAPI apiService;
	private static Log log = LogFactory.getLog(SampleFormController.class);

	@Autowired
	public SampleFormController(SampleAPI apiService) {
		this.apiService = apiService;
	}
	
	@RequestMapping(method = RequestMethod.GET)
	public String setupForm(Model model) {
		log.info("setupForm()");
		SampleDao dao = new SampleDao("Put Your Value here");
		model.addAttribute("dao", dao);
		return "sample/getDaoForm";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String processSubmit( @ModelAttribute("dao") SampleDao dao,  BindingResult result, SessionStatus status, HttpSession session) {
		log.info("calling OSGi apiService.add()");
		apiService.add(dao);
		log.info("back from OSGi apiService.add()");
		session.setAttribute("message", "called apiService.add(" +dao.getText() + ")");
		return "redirect:/sample/index.htm";
	}
}
