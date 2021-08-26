package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/validation/v3/items")
@RequiredArgsConstructor //생성자 하나면 자동으로 생성자 주입
public class ValidationItemControllerV3 {

    private final ItemRepository itemRepository;

      //@RequiredArgsConstructor있어서 생략가능
//    @Autowired //생성자1개일땐 autowired 생략가능
//    public ValidationItemControllerV2(ItemRepository itemRepository) {
//        this.itemRepository = itemRepository;
//    }

    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "validation/v3/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v3/item";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        //빈 값을 넘긴 이유는 검증에 실패했을때 데이터 넘어간게 다시보이도록 재사용할 수 있다.
        return "validation/v3/addForm";
    }


     @PostMapping("/add")
    public String addItem(@Validated @ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
//        @Validated: item에대해서 자동으로 검증기가 수행된다.
//        @Validated 는 검증기를 실행하라는 애노테이션이다.
//        이 애노테이션이 붙으면 앞서 WebDataBinder 에 등록한 검증기를 찾아서 실행한다. 그런데 여러 검증기를
//        등록한다면 그 중에 어떤 검증기가 실행되어야 할지 구분이 필요하다. 이때 supports() 가 사용된다.
//        여기서는 supports(Item.class) 호출되고, 결과가 true 이므로 ItemValidator 의 validate() 가
//        호출된다.
        //검증이 끝나면 검증 결과가 bindingResult에 담겨진다.

        //검증에 실패하면 다시 입력 폼으로
        if(bindingResult.hasErrors()){
            log.info("error ={}", bindingResult);
            // model.addAttribute("errors", errors);
            //bindingResult는 자동으로 뷰에 넘어가기 때문에 굳이 model.addAttribute에 넣지 않아도 된다.
            return  "validation/v3/addForm";

        }

        //성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v3/items/{itemId}";
    }


    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v3/editForm";
    }

    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @ModelAttribute Item item) {
        itemRepository.update(itemId, item);
        return "redirect:/validation/v3/items/{itemId}";
    }

}

