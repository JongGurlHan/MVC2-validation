package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/validation/v2/items")
@RequiredArgsConstructor //생성자 하나면 자동으로 생성자 주입
public class ValidationItemControllerV2 {

    private final ItemRepository itemRepository;
    private final ItemValidator itemValidator; //스프링에서 빈 주입

    //컨트롤러가 호출될때마다 항상 불려져서 WebDataBinder가 매번 만들어지는데,그때  itemValidator를 넣어둔다.
    // 이렇게 WebDataBinder 에 검증기를 추가하면 해당 컨트롤러에서는 검증기를 자동으로 적용할 수 있다.
    // @InitBinder 해당 컨트롤러에만 영향을 준다. 글로벌 설정은 별도로 해야한다.
    @InitBinder
    public void init(WebDataBinder dataBinder){
        dataBinder.addValidators(itemValidator);
    }

    //@RequiredArgsConstructor있어서 생략가능
//    @Autowired //생성자1개일땐 autowired 생략가능
//    public ValidationItemControllerV2(ItemRepository itemRepository, ItemValidator itemValidator) {
//        this.itemRepository = itemRepository;
//        this.itemValidator = itemValidator;
//    }

    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "validation/v2/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v2/item";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        //빈 값을 넘긴 이유는 검증에 실패했을때 데이터 넘어간게 다시보이도록 재사용할 수 있다.
        return "validation/v2/addForm";
    }

//    @PostMapping("/add")
    public String addItemV1(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
    //item객체에 바인딩 된 결과과 bindingResult에 담기기 때문에 BindingResult는 ModelAttribute 바로뒤에 가야한다.

        //bindingResult가 errors 역할을 해준다. 스프링이 제공하는 메커니즘
        //binding: 검증오류를 보관하는 객체, @ModelAttribute에 데이터 바인딩시 오류가 발생해도 컨트롤러가 호출된다.
        //BindingResult가 없으면 -> 400 오류 발생하면서 컨트롤러 호출되지 않고 오류페이지로 이동
        //BindingResult가 있으면 -> 오류정보('FieldError')를 BindingResult에 담아서 컨트롤러를 정상 호출한다.

        //검증 로직(필드 룰)
        if(!StringUtils.hasText(item.getItemName())){
            bindingResult.addError(new FieldError("item", "itemName", "상품이름은 필수입니다."));
        }
        if(item.getPrice() == null || item.getPrice() < 1000  || item.getPrice() > 1000000){
            bindingResult.addError(new FieldError("item", "price", "가격은 1,000 ~ 1,000,000 까지 허용합니다."));
        }
        if(item.getQuantity() == null || item.getQuantity() >= 9999){
            bindingResult.addError(new FieldError("item", "quantity", "수량은 최대 9,999 까지 허용합니다."));
        }

        //특정 필드가 아닌 복합 룰 검증
        if(item.getPrice() != null && item.getQuantity() !=null){
            int resultPrice = item.getPrice() * item.getQuantity();
            if(resultPrice <10000){
                bindingResult.addError(new ObjectError("item", "가격 * 수량의 합은 10,000원 이상어야 합니다. 현재값: " + resultPrice));                         //특정 필드의 오류가 아닌 global 오류이기 때문
                //특정 필드의 오류가 아닌 global 오류이기 때문
            }
        }

        //검증에 실패하면 다시 입력 폼으로
        if(bindingResult.hasErrors()){
            log.info("error ={}", bindingResult);
           // model.addAttribute("errors", errors);
            //bindingResult는 자동으로 뷰에 넘어가기 때문에 굳이 model.addAttribute에 넣지 않아도 된다.
            return  "validation/v2/addForm";

        }

        //성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

//    @PostMapping("/add")
    public String addItemV2(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {

        //bindingResult가 errors 역할을 해준다. 스프링이 제공하는 메커니즘
        //binding: 검증오류를 보관하는 객체, @ModelAttribute에 데이터 바인딩시 오류가 발생해도 컨트롤러가 호출된다.
        //BindingResult가 없으면 -> 400 오류 발생하면서 컨트롤러 호출되지 않고 오류페이지로 이동
        //BindingResult가 있으면 -> 오류정보('FieldError')를 BindingResult에 담아서 컨트롤러를 정상 호출한다.

        //검증 로직(필드 룰)
        if(!StringUtils.hasText(item.getItemName())){
            bindingResult.addError(new FieldError("item", "itemName", item.getItemName(),false, null,null, "상품이름은 필수입니다."));
            //FieldError에는 생성자가 2개 있다.
            //rejectedValue – the rejected field value
            //bindingFailure – whether this error represents a binding failure (like a type mismatch); else, it is a validation failure, 데이터 자체가 넘어가는게 실패했는지
            //codes – the codes to be used to resolve this message, 메시지 코드
            //arguments – the array of arguments to be used to resolve this message, 메시지에서 사용하는 인자
        }
        if(item.getPrice() == null || item.getPrice() < 1000  || item.getPrice() > 1000000){
            bindingResult.addError(new FieldError("item", "price", item.getPrice(),false,null,null, "가격은 1,000 ~ 1,000,000 까지 허용합니다."));
        }
        if(item.getQuantity() == null || item.getQuantity() >= 9999){
            bindingResult.addError(new FieldError("item", "quantity", item.getQuantity(),false,null,null, "수량은 최대 9,999 까지 허용합니다."));
        }

        //특정 필드가 아닌 복합 룰 검증
        if(item.getPrice() != null && item.getQuantity() !=null){
            int resultPrice = item.getPrice() * item.getQuantity();
            if(resultPrice <10000){
                bindingResult.addError(new ObjectError("item", null, null, "가격 * 수량의 합은 10,000원 이상어야 합니다. 현재값: " + resultPrice));                         //특정 필드의 오류가 아닌 global 오류이기 때문
                //특정 필드의 오류가 아닌 global 오류이기 때문
            }
        }

        //검증에 실패하면 다시 입력 폼으로
        if(bindingResult.hasErrors()){
            log.info("error ={}", bindingResult);
            // model.addAttribute("errors", errors);
            //bindingResult는 자동으로 뷰에 넘어가기 때문에 굳이 model.addAttribute에 넣지 않아도 된다.
            return  "validation/v2/addForm";

        }

        //성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

   // @PostMapping("/add")
    public String addItemV3(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {

        log.info("objectName={}", bindingResult.getObjectName()); //
        log.info("target={}", bindingResult.getTarget());
        //bindingResult가 errors 역할을 해준다. 스프링이 제공하는 메커니즘
        //binding: 검증오류를 보관하는 객체, @ModelAttribute에 데이터 바인딩시 오류가 발생해도 컨트롤러가 호출된다.
        //BindingResult가 없으면 -> 400 오류 발생하면서 컨트롤러 호출되지 않고 오류페이지로 이동
        //BindingResult가 있으면 -> 오류정보('FieldError')를 BindingResult에 담아서 컨트롤러를 정상 호출한다.

        //검증 로직(필드 룰)
        if(!StringUtils.hasText(item.getItemName())){
            bindingResult.addError(new FieldError("item", "itemName", item.getItemName(),false, new String[]{"required.item.itemName"},null, null));
            //FieldError에는 생성자가 2개 있다.
            //rejectedValue – the rejected field value
            //bindingFailure – whether this error represents a binding failure (like a type mismatch); else, it is a validation failure, 데이터 자체가 넘어가는게 실패했는지
            //codes – the codes to be used to resolve this message, 메시지 코드
            //arguments – the array of arguments to be used to resolve this message, 메시지에서 사용하는 인자
        }
        if(item.getPrice() == null || item.getPrice() < 1000  || item.getPrice() > 1000000){
            bindingResult.addError(new FieldError("item", "price", item.getPrice(),false,new String[]{"range.item.price"},new Object[]{1000, 1000000}, null));
        }
        if(item.getQuantity() == null || item.getQuantity() >= 9999){
            bindingResult.addError(new FieldError("item", "quantity", item.getQuantity(),false, new String[]{"max.item.quantity"},new Object[]{9999}, null));
        }

        //특정 필드가 아닌 복합 룰 검증
        if(item.getPrice() != null && item.getQuantity() !=null){
            int resultPrice = item.getPrice() * item.getQuantity();
            if(resultPrice <10000){
                bindingResult.addError(new ObjectError("item", new String[]{"totalPriceMin"}, new Object[]{10000, resultPrice},null));   //특정 필드의 오류가 아닌 global 오류이기 때문
                //특정 필드의 오류가 아닌 global 오류이기 때문
            }
        }

        //검증에 실패하면 다시 입력 폼으로
        if(bindingResult.hasErrors()){
            log.info("error ={}", bindingResult);
            // model.addAttribute("errors", errors);
            //bindingResult는 자동으로 뷰에 넘어가기 때문에 굳이 model.addAttribute에 넣지 않아도 된다.
            return  "validation/v2/addForm";

        }

        //성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

    //@PostMapping("/add")
    public String addItemV4(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {

        log.info("objectName={}", bindingResult.getObjectName()); //
        log.info("target={}", bindingResult.getTarget());

        //rejectValue(), reject(): FieldError, ObjectError를 직접 생성하지 않고 깔끔하게 검증 오류를 다룰 수 있다.
        //검증 로직(필드 룰)
        if(!StringUtils.hasText(item.getItemName())){
//            bindingResult.addError(new FieldError("item", "itemName", item.getItemName(),false, new String[]{"required.item.itemName"},null, null));
            bindingResult.rejectValue("itemName", "required");
            //objectName은 이미 bindingResult가 알고있다.
            //errorCode는 규칙이 있다!
        }
        if(item.getPrice() == null || item.getPrice() < 1000  || item.getPrice() > 1000000){
           bindingResult.rejectValue("price", "range", new Object[]{1000, 1000000}, null);
        }
        if(item.getQuantity() == null || item.getQuantity() >= 9999){
            bindingResult.rejectValue("quantity", "max", new Object[]{9999}, null);
        }

        //특정 필드가 아닌 복합 룰 검증
        if(item.getPrice() != null && item.getQuantity() !=null){
            int resultPrice = item.getPrice() * item.getQuantity();
            if(resultPrice <10000){
                //특정 필드의 오류가 아닌 global 오류이기 때문
                bindingResult.reject("totalPriceMin" , new Object[]{10000, resultPrice},null);
            }
        }

        //검증에 실패하면 다시 입력 폼으로
        if(bindingResult.hasErrors()){
            log.info("error ={}", bindingResult);
            // model.addAttribute("errors", errors);
            //bindingResult는 자동으로 뷰에 넘어가기 때문에 굳이 model.addAttribute에 넣지 않아도 된다.
            return  "validation/v2/addForm";

        }

        //성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

  //  @PostMapping("/add")
    public String addItemV5(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {

        itemValidator.validate(item, bindingResult); //검증 로직 가져오기

        //검증에 실패하면 다시 입력 폼으로
        if(bindingResult.hasErrors()){
            log.info("error ={}", bindingResult);
            // model.addAttribute("errors", errors);
            //bindingResult는 자동으로 뷰에 넘어가기 때문에 굳이 model.addAttribute에 넣지 않아도 된다.
            return  "validation/v2/addForm";

        }

        //성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

     @PostMapping("/add")
    public String addItemV6(@Validated @ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
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
            return  "validation/v2/addForm";

        }

        //성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }


    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v2/editForm";
    }

    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @ModelAttribute Item item) {
        itemRepository.update(itemId, item);
        return "redirect:/validation/v2/items/{itemId}";
    }

}

