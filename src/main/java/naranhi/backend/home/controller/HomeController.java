package naranhi.backend.home.controller;

import lombok.RequiredArgsConstructor;
import naranhi.backend.global.response.ApiResponse;
import naranhi.backend.home.dto.HomeResponse;
import naranhi.backend.home.service.HomeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/home")
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    @GetMapping
    public ApiResponse<HomeResponse.Home> getHome(@RequestParam Long memberId) {
        return ApiResponse.ok(homeService.getHome(memberId));
    }
}
