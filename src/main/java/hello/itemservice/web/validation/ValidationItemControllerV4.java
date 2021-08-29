package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import hello.itemservice.domain.item.SaveCheck;
import hello.itemservice.domain.item.UpdateCheck;
import hello.itemservice.web.validation.form.ItemSaveForm;
import hello.itemservice.web.validation.form.ItemUpdateForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/validation/v4/items")
@RequiredArgsConstructor //생성자 하나면 자동으로 생성자 주입
public class ValidationItemControllerV4 {

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
        return "validation/v4/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v4/item";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        //빈 값을 넘긴 이유는 검증에 실패했을때 데이터 넘어간게 다시보이도록 재사용할 수 있다.
        return "validation/v4/addForm";
    }


    @PostMapping("/add")
    public String addItem(@Validated @ModelAttribute("item") ItemSaveForm form, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        //(item)안넣어주면 itemSaveForm(객체명)으로 자동으로 들어간다.
        //model.addAttribute("itemSaveForm", form) 으로 들어간다는 말이다.
//        @ModelAttribute("item") 에 item 이름을 넣어준 부분을 주의하자. 이것을 넣지 않으면
//        ItemSaveForm 의 경우 규칙에 의해 itemSaveForm 이라는 이름으로 MVC Model에 담기게 된다.
//        이렇게 되면 뷰 템플릿에서 접근하는 th:object 이름도 함께 변경해주어야 한다.


        //addForm에서 입력한 내용들이 @ModelAttribute을 보고 ItemSaveForm에 에 값이 쌓인다
        //이후 model.addAttribute("item", form)해줘야하니까
        //특정 필드가 아닌 복합 룰 검증
        if(form.getPrice() != null && form.getQuantity() !=null){
            int resultPrice = form.getPrice() * form.getQuantity();
            if(resultPrice <10000){
                //특정 필드의 오류가 아닌 global 오류이기 때문
                bindingResult.reject("totalPriceMin" , new Object[]{10000, resultPrice},null);
            }
        }

        //검증에 실패하면 다시 입력 폼으로
        if(bindingResult.hasErrors()){
            log.info("error ={}", bindingResult);
            return  "validation/v4/addForm";

        }

        //성공 로직

        Item item = new Item();
        item.setItemName(form.getItemName());
        item.setPrice(form.getPrice());
        item.setQuantity(form.getQuantity());


        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v4/items/{itemId}";
    }


    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v4/editForm";
    }

    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @Validated @ModelAttribute("item") ItemUpdateForm form, BindingResult bindingResult) {

        //특정 필드가 아닌 복합 룰 검증
        if(form.getPrice() != null && form.getQuantity() !=null){
            int resultPrice = form.getPrice() * form.getQuantity();
            if(resultPrice <10000){
                //특정 필드의 오류가 아닌 global 오류이기 때문
                bindingResult.reject("totalPriceMin" , new Object[]{10000, resultPrice},null);
            }
        }

        //검증에 실패하면 다시 입력 폼으로
        if(bindingResult.hasErrors()){
            log.info("error ={}", bindingResult);
            return  "validation/v4/editForm";
        }

        Item itemParam = new Item();
        itemParam.setItemName(form.getItemName());
        itemParam.setPrice(form.getPrice());
        itemParam.setQuantity(form.getQuantity());

        itemRepository.update(itemId, itemParam);
        return "redirect:/validation/v4/items/{itemId}";
    }

}

