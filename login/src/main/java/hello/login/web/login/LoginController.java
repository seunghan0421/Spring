package hello.login.web.login;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import hello.login.domain.login.LoginService;
import hello.login.domain.member.Member;
import hello.login.web.session.SessionConst;
import hello.login.web.session.SessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LoginController {

	private final LoginService loginService;
	private final SessionManager sessionManager;

	@GetMapping("/login")
	public String loginForm(@ModelAttribute("loginForm") LoginForm form) {
		return "login/loginForm";
	}

	// @PostMapping("/login")
	public String login(@Valid @ModelAttribute("loginForm") LoginForm form, BindingResult bindingResult,
		HttpServletResponse response) {

		if (bindingResult.hasErrors()) {
			return "login/loginForm";
		}

		final Member loginMember = loginService.login(form.getLoginId(), form.getPassword());

		log.info("login =  {}", loginMember);

		if (loginMember == null) {
			bindingResult.reject("loginFail", "아이디 또는 비밀번호가 맞지 않습니다.");
			return "login/loginForm";
		}

		// login 처리 TODO
		// 여기서 쿠키를 만들어서 전달 해 줄것이다.
		final Cookie idCookie = new Cookie("memberId", String.valueOf(loginMember.getId()));
		response.addCookie(idCookie);

		return "redirect:/";
	}

	// @PostMapping("/login")
	public String loginV2(@Valid @ModelAttribute("loginForm") LoginForm form, BindingResult bindingResult,
		HttpServletResponse response) {
		if (bindingResult.hasErrors()) {
			return "login/loginForm";
		}

		final Member loginMember = loginService.login(form.getLoginId(), form.getPassword());
		log.info("login? {}", loginMember);

		if (loginMember == null) {
			bindingResult.reject("loginFail", "아이디 또는 비밀번호가 일치하지 않습니다.");
			return "login/loginForm";
		}

		// 로그인 성공 처리
		sessionManager.createSession(loginMember, response);

		return "redirect:/";

	}

	@PostMapping("/login")
	public String loginV3(@Valid @ModelAttribute("loginForm") LoginForm form, BindingResult bindingResult,
		HttpServletRequest request) {

		if (bindingResult.hasErrors()) {
			return "login/loginForm";
		}

		final Member loginMember = loginService.login(form.getLoginId(), form.getPassword());
		log.info("login? {}", loginMember);

		if (loginMember == null) {
			bindingResult.reject("loginFail", "아이디 또는 비밀번호가 일치하지 않습니다.");
			return "login/loginForm";
		}

		// 세션이 있으면 세션 반환, 없으면 신규 세션을 생성
		final HttpSession session = request.getSession();
		// 세션에 로그인 회원 정보 보관
		session.setAttribute(SessionConst.LOGIN_MEMBER, loginMember);

		// 로그인 성공 처리
		// sessionManager.createSession(loginMember, response);


		return "redirect:/";

	}

	// @PostMapping("/logout")
	public String logout(HttpServletResponse response) {
		expireCookie(response, "memberId");

		return "redirect:/";
	}

	// @PostMapping("/logout")
	public String logoutV2(HttpServletRequest request) {
		sessionManager.expire(request);

		return "redirect:/";
	}

	@PostMapping("/logout")
	public String logoutV3(HttpServletRequest request) {
		final HttpSession session = request.getSession();
		if (session != null) {
			session.invalidate();
		}

		return "redirect:/";
	}

	private void expireCookie(HttpServletResponse response, String cookieName) {
		final Cookie cookie = new Cookie(cookieName, null);
		cookie.setMaxAge(0);
		response.addCookie(cookie);
	}

}
